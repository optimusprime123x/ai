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

package com.google.ai.edge.gallery.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** The "Stars 2" AI sparkle icon from Material Symbols Outlined. */
val Stars_2: ImageVector
  get() {
    if (internal_Stars_2 != null) {
      return internal_Stars_2!!
    }
    internal_Stars_2 =
      ImageVector.Builder(
          name = "Stars_2",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1.0f,
            stroke = null,
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero,
          ) {
            moveTo(8.85f, 16.83f)
            lineTo(12f, 14.93f)
            lineToRelative(3.15f, 1.93f)
            lineToRelative(-0.82f, -3.6f)
            lineToRelative(2.78f, -2.4f)
            lineTo(13.45f, 10.52f)
            lineTo(12f, 7.13f)
            lineTo(10.55f, 10.5f)
            lineTo(6.9f, 10.83f)
            lineToRelative(2.78f, 2.43f)
            lineTo(8.85f, 16.83f)
            close()
            moveTo(5.83f, 21f)
            lineTo(7.45f, 13.98f)
            lineTo(2f, 9.25f)
            lineTo(9.2f, 8.63f)
            lineTo(12f, 2f)
            lineToRelative(2.8f, 6.63f)
            lineTo(22f, 9.25f)
            lineToRelative(-5.45f, 4.72f)
            lineTo(18.18f, 21f)
            lineTo(12f, 17.27f)
            lineTo(5.83f, 21f)
            close()
            moveTo(17.25f, 7f)
            lineTo(17.78f, 4.77f)
            lineTo(16f, 3.3f)
            lineTo(18.35f, 3.1f)
            lineTo(19.25f, 1f)
            lineToRelative(0.9f, 2.1f)
            lineTo(22.5f, 3.3f)
            lineTo(20.73f, 4.77f)
            lineTo(21.25f, 7f)
            lineToRelative(-2f, -1.18f)
            lineTo(17.25f, 7f)
            close()
            moveTo(12f, 11.98f)
            close()
          }
        }
        .build()
    return internal_Stars_2!!
  }

private var internal_Stars_2: ImageVector? = null
