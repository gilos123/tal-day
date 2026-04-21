package com.maayan.studytracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Paints a solid-color "hard" shadow (no blur) underneath the composable. Matches the
 * Duolingo / Habitica feel where buttons have a 2dp offset under-layer instead of a
 * Material elevation blur.
 *
 * Typical usage:
 *   Button(
 *       modifier = Modifier.hardShadow(
 *           offsetY = 3.dp,
 *           color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
 *           shape = MaterialTheme.shapes.medium
 *       ),
 *       ...
 *   )
 */
fun Modifier.hardShadow(
    offsetY: Dp = 2.dp,
    color: Color,
    shape: Shape = RoundedCornerShape(14.dp)
): Modifier = this.drawBehind {
    val outline: Outline = shape.createOutline(
        size = Size(size.width, size.height),
        layoutDirection = layoutDirection,
        density = this
    )
    translate(left = 0f, top = offsetY.toPx()) {
        when (outline) {
            is Outline.Rectangle -> drawRect(color = color, topLeft = Offset.Zero, size = size)
            is Outline.Rounded -> drawPath(
                path = androidx.compose.ui.graphics.Path().apply { addRoundRect(outline.roundRect) },
                color = color
            )
            is Outline.Generic -> drawPath(path = outline.path, color = color)
        }
    }
}
