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

package com.google.ai.edge.gallery.ui.common.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.R
import java.util.Locale

/**
 * Composable function to display the live generation speed (in tokens per second) of a model
 * response, along with a small speedometer icon.
 *
 * @param tokensPerSecond The current generation speed. Non-finite or negative values hide the
 *   indicator.
 */
@Composable
fun GenerationSpeedIndicator(tokensPerSecond: Float, modifier: Modifier = Modifier) {
  if (!tokensPerSecond.isFinite() || tokensPerSecond < 0f) {
    return
  }
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(3.dp),
    modifier = modifier.alpha(0.5f).testTag("generation_speed_indicator"),
  ) {
    Icon(
      imageVector = Icons.Rounded.Speed,
      contentDescription = stringResource(R.string.cd_generation_speed_icon),
      tint = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.size(12.dp),
    )
    Text(
      String.format(Locale.US, "%.1f t/s", tokensPerSecond),
      style = MaterialTheme.typography.labelSmall,
    )
  }
}
