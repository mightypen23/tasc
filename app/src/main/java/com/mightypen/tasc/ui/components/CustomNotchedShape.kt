package com.mightypen.tasc.ui.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Custom shape matching the exact notched card silhouette in the design screenshot:
 * - Rounded tab on top-left
 * - Smooth concave fillet curve dropping into the horizontal shelf
 * - Large rounded corners for the main card body
 */
class NotchedCardShape(
    private val tabWidth: Dp = 104.dp,
    private val tabHeight: Dp = 34.dp,
    private val cornerRadius: Dp = 24.dp,
    private val filletRadius: Dp = 14.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height

        val tw = with(density) { tabWidth.toPx().coerceAtMost(w * 0.42f) }
        val th = with(density) { tabHeight.toPx().coerceAtMost(h * 0.35f) }
        val r = with(density) { cornerRadius.toPx() }
        val f = with(density) { filletRadius.toPx() }

        val path = Path().apply {
            // Start at top-left below the corner
            moveTo(0f, r)

            // Top-left tab corner
            arcTo(
                rect = Rect(0f, 0f, 2 * r, 2 * r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Top edge of the tab
            lineTo(tw - r, 0f)

            // Top-right corner of the tab
            arcTo(
                rect = Rect(tw - 2 * r, 0f, tw, 2 * r),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Right vertical edge of the tab going down
            lineTo(tw, th - f)

            // Smooth concave fillet transition: from (tw, th - f) curving down to (tw + f, th)
            // Center is at (tw + f, th - f), radius is f.
            // Angle 180 deg is (tw, th - f), angle 90 deg is (tw + f, th).
            arcTo(
                rect = Rect(tw, th - 2 * f, tw + 2 * f, th),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )

            // Horizontal shelf edge going to the right
            lineTo(w - r, th)

            // Top-right corner of the main card
            arcTo(
                rect = Rect(w - 2 * r, th, w, th + 2 * r),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Right edge of main card
            lineTo(w, h - r)

            // Bottom-right corner
            arcTo(
                rect = Rect(w - 2 * r, h - 2 * r, w, h),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Bottom edge
            lineTo(r, h)

            // Bottom-left corner
            arcTo(
                rect = Rect(0f, h - 2 * r, 2 * r, h),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Left edge back to start
            lineTo(0f, r)
            close()
        }

        return Outline.Generic(path)
    }
}
