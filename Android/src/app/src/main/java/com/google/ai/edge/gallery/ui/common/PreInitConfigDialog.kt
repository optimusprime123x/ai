/*
 * Copyright 2026 The AI Edge Gallery Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.edge.gallery.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.ai.edge.gallery.R
import com.google.ai.edge.gallery.customtasks.agentchat.agentSkillTopK
import com.google.ai.edge.gallery.customtasks.agentchat.agentSkillTopKAdjusted
import com.google.ai.edge.gallery.data.BuiltInTaskId
import com.google.ai.edge.gallery.data.ConfigKeys
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.data.ModelCapability
import com.google.ai.edge.gallery.data.Task
import com.google.ai.edge.gallery.ui.modelmanager.ModelManagerViewModel

/**
 * Displays the configuration dialog for a model before it is initialized.
 *
 * This dialog is shown right before a model's first initialization so the user can tweak sampling
 * params, thinking, speculative decoding, etc. Pressing OK applies the values, persists them as
 * the model's defaults, and then triggers the initialization via [onConfirmed]. Dismissing the
 * dialog (tapping outside) proceeds with the current values unchanged.
 */
@Composable
fun PreInitConfigDialog(
  task: Task,
  model: Model,
  modelManagerViewModel: ModelManagerViewModel,
  onConfirmed: () -> Unit,
) {
  val context = LocalContext.current

  // Computed once per model: probing the model file for capabilities opens it natively, so it
  // must not run on every recomposition while the dialog is visible.
  val modelConfigs =
    remember(model.name) {
      val configs = model.configs.toMutableList()
      if (!task.allowCapability(ModelCapability.LLM_THINKING, model)) {
        configs.removeIf { it.key == ConfigKeys.ENABLE_THINKING }
      }
      var supportsSpeculativeDecoding = false
      // Check if the model file supports speculative decoding.
      try {
        com.google.ai.edge.litertlm.Capabilities(model.getPath(context)).use {
          supportsSpeculativeDecoding = it.hasSpeculativeDecodingSupport()
        }
      } catch (e: Exception) {
        // Ignore exceptions and assume not supported.
      }
      if (
        !supportsSpeculativeDecoding ||
          !task.allowCapability(ModelCapability.SPECULATIVE_DECODING, model)
      ) {
        configs.removeIf { it.key == ConfigKeys.ENABLE_SPECULATIVE_DECODING }
      }
      configs
    }

  ConfigDialog(
    title = stringResource(R.string.config_dialog_title),
    subtitle = stringResource(R.string.config_dialog_pre_init_subtitle),
    configs = modelConfigs,
    initialValues = model.configValues,
    showCancel = false,
    // Dismissing (tapping outside) proceeds with the unchanged config values.
    onDismissed = onConfirmed,
    onOk = { curConfigValues, _, _ ->
      // Apply and persist the config values, then initialize the model.
      model.configValues = curConfigValues
      modelManagerViewModel.saveModelConfigValues(model = model)
      // Mirror the app-bar config dialog's Agent Skills TopK bookkeeping so a TopK chosen here
      // isn't silently reset to greedy on the next task entry.
      if (task.id == BuiltInTaskId.LLM_AGENT_CHAT) {
        model.agentSkillTopKAdjusted = true
        model.agentSkillTopK = curConfigValues[ConfigKeys.TOPK.label]
      }
      modelManagerViewModel.updateConfigValuesUpdateTrigger()
      onConfirmed()
    },
  )
}
