package com.mightypen.tasc.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mightypen.tasc.ui.theme.CardIconContainer

@Composable
fun TaskIconSquircle(
    tintColor: Color,
    size: Dp = 46.dp,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "squircleScale"
    )

    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(CardIconContainer)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.58f)) {
            val strokeWidth = 2.4.dp.toPx()
            val w = this.size.width
            val h = this.size.height

            // Row 1: Checkmark + line
            // Checkmark 1
            val check1Path = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.12f, h * 0.32f)
                lineTo(w * 0.28f, h * 0.46f)
                lineTo(w * 0.46f, h * 0.20f)
            }
            drawPath(
                path = check1Path,
                color = tintColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Task line 1
            drawLine(
                color = tintColor,
                start = Offset(w * 0.60f, h * 0.33f),
                end = Offset(w * 0.90f, h * 0.33f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Row 2: Checkmark + line
            // Checkmark 2
            val check2Path = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.12f, h * 0.72f)
                lineTo(w * 0.28f, h * 0.86f)
                lineTo(w * 0.46f, h * 0.60f)
            }
            drawPath(
                path = check2Path,
                color = tintColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Task line 2
            drawLine(
                color = tintColor,
                start = Offset(w * 0.60f, h * 0.73f),
                end = Offset(w * 0.90f, h * 0.73f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
