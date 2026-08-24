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

package com.google.ai.edge.gallery.ui.common.modelitem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.Recommend
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.R
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.ui.common.getDeviceMemInGb

/**
 * Composable function to display small capability badges for a model, such as "Vision" for models
 * with image input support and "Uncensored" for abliterated model variants.
 */
@Composable
fun ModelBadges(model: Model, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val deviceMemInGb = remember { getDeviceMemInGb(context) }
  val recommended =
    remember(model.name) {
      val minGb = model.recommendedRamMinGb
      deviceMemInGb != null &&
        minGb != null &&
        deviceMemInGb >= minGb &&
        (model.recommendedRamMaxGb == null || deviceMemInGb < model.recommendedRamMaxGb)
    }
  if (!model.llmSupportImage && !model.uncensored && !recommended) {
    return
  }
  Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
    if (recommended) {
      ModelBadge(
        icon = Icons.Rounded.Recommend,
        label = stringResource(R.string.model_badge_recommended),
        iconTint = MaterialTheme.colorScheme.tertiary,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        textColor = MaterialTheme.colorScheme.onTertiaryContainer,
      )
    }
    if (model.llmSupportImage) {
      ModelBadge(
        icon = Icons.Outlined.Visibility,
        label = stringResource(R.string.model_badge_vision),
        iconTint = MaterialTheme.colorScheme.primary,
      )
    }
    if (model.uncensored) {
      ModelBadge(
        icon = Icons.Rounded.WarningAmber,
        label = stringResource(R.string.model_badge_uncensored),
        iconTint = MaterialTheme.colorScheme.error,
      )
    }
  }
}

@Composable
private fun ModelBadge(
  icon: ImageVector,
  label: String,
  iconTint: Color,
  containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
  textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(3.dp),
    modifier =
      Modifier.clip(RoundedCornerShape(6.dp))
        .background(containerColor)
        .padding(horizontal = 6.dp, vertical = 2.dp),
  ) {
    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(12.dp))
    Text(label, style = MaterialTheme.typography.labelSmall, color = textColor)
  }
}
