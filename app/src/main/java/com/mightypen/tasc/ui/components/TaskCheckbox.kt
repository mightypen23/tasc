package com.mightypen.tasc.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class SparkleParticle(
    val angle: Double,
    val distance: Float,
    val size: Float,
    val color: Color
)

@Composable
fun TaskCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Guard so celebration confetti & pop ONLY trigger when the user actually checks THIS task,
    // and NEVER on initial screen load or when other tasks update.
    var hasInitialized by remember { mutableStateOf(false) }
    var wasChecked by remember { mutableStateOf(checked) }

    val popScale = remember { Animatable(1f) }
    val particleProgress = remember { Animatable(0f) }

    LaunchedEffect(checked) {
        if (!hasInitialized) {
            hasInitialized = true
            wasChecked = checked
            return@LaunchedEffect
        }
        if (checked && !wasChecked) {
            // User specifically tapped check on this task!
            popScale.snapTo(0.82f)
            popScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            particleProgress.snapTo(0f)
            particleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        } else {
            particleProgress.snapTo(0f)
        }
        wasChecked = checked
    }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
        label = "pressScale"
    )

    // Circular checkbox background color
    val checkboxCircleBg by animateColorAsState(
        targetValue = if (checked) accentColor else Color(0xFF0D1624),
        animationSpec = tween(durationMillis = 240),
        label = "checkboxCircleBg"
    )

    // Circular checkbox border color - high contrast so it's unmistakably a checkbox
    val checkboxCircleBorder by animateColorAsState(
        targetValue = if (checked) accentColor else Color(0xFF6F89AF),
        animationSpec = tween(durationMillis = 240),
        label = "checkboxCircleBorder"
    )

    // Checkmark icon color: solid bold charcoal when checked, clear visible ghost tint when unchecked
    val checkIconTint by animateColorAsState(
        targetValue = if (checked) Color(0xFF101B2B) else Color.White.copy(alpha = 0.50f),
        animationSpec = tween(durationMillis = 200),
        label = "checkIconTint"
    )

    // Pre-calculate celebratory particles
    val particles = remember(accentColor) {
        val colors = listOf(
            accentColor,
            Color(0xFFFFD54F), // Radiant gold
            Color(0xFF5DE2E7), // Vibrant cyan
            Color(0xFFFFFFFF), // White star
            Color(0xFFD6F58F), // Lime
            Color(0xFFFF8A65)  // Coral
        )
        val rnd = Random(42)
        List(12) { i ->
            SparkleParticle(
                angle = (i * (360.0 / 12.0) + rnd.nextDouble(-12.0, 12.0)) * (Math.PI / 180.0),
                distance = rnd.nextFloat() * 26f + 24f,
                size = rnd.nextFloat() * 3.5f + 2.5f,
                color = colors[i % colors.size]
            )
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(pressScale * popScale.value)
            .semantics {
                role = Role.Checkbox
                contentDescription = if (checked) "Mark task as incomplete" else "Mark task as complete"
            },
        contentAlignment = Alignment.Center
    ) {
        // Celebratory particle sparkles layer (ONLY runs on this specific task when checked)
        if (particleProgress.value > 0f && particleProgress.value < 1f) {
            Canvas(modifier = Modifier.size(size * 2.4f)) {
                val centerOffset = Offset(this.size.width / 2f, this.size.height / 2f)
                val prog = particleProgress.value
                val alpha = (1f - prog).coerceIn(0f, 1f)

                particles.forEach { p ->
                    val distPx = (p.distance * prog).dp.toPx()
                    val px = centerOffset.x + (cos(p.angle) * distPx).toFloat()
                    val py = centerOffset.y + (sin(p.angle) * distPx).toFloat()
                    val radiusPx = (p.size * (1f - prog * 0.4f)).dp.toPx()

                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = radiusPx,
                        center = Offset(px, py)
                    )
                }
            }
        }

        // Outer Dark Pod Button (Generous touch target with sleek rounded squircle shape)
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF131D2E))
                .border(
                    width = 1.dp,
                    color = if (checked) accentColor.copy(alpha = 0.5f) else Color(0xFF22344B),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCheckedChange()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Distinct, High-Affordance Circular Checkbox
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(checkboxCircleBg)
                    .border(
                        width = 2.dp,
                        color = checkboxCircleBorder,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Checkmark icon: Clearly visible at all times!
                // When unchecked, clearly shows a visible checkmark affordance so no one has to guess.
                // When checked, shows a bold, high-contrast checkmark.
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = checkIconTint,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
