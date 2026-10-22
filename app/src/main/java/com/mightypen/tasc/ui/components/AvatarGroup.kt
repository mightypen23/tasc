package com.mightypen.tasc.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mightypen.tasc.ui.theme.AppBackgroundDark

private val AvatarGradients = listOf(
    Brush.linearGradient(listOf(Color(0xFFFFB37E), Color(0xFFF77245))), // Warm orange/amber
    Brush.linearGradient(listOf(Color(0xFF90CAF9), Color(0xFF42A5F5))), // Cool blue
    Brush.linearGradient(listOf(Color(0xFFA5D6A7), Color(0xFF43A047))), // Green
    Brush.linearGradient(listOf(Color(0xFFCE93D8), Color(0xFFAB47BC))), // Purple
)

@Composable
fun AvatarGroup(
    count: Int = 3,
    avatarSize: Dp = 26.dp,
    overlapOffset: Dp = (-8).dp,
    onAddClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "avatarScale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.scale(scale)
    ) {
        val displayCount = count.coerceIn(1, 4)
        for (i in 0 until displayCount) {
            Box(
                modifier = Modifier
                    .offset(x = overlapOffset * i)
                    .size(avatarSize)
                    .border(1.5.dp, AppBackgroundDark, CircleShape)
                    .clip(CircleShape)
                    .background(AvatarGradients[i % AvatarGradients.size]),
                contentAlignment = Alignment.Center
            ) {
                // Stylized avatar icon representing team members
                Icon(
                    imageVector = if (i % 2 == 0) Icons.Filled.Person else Icons.Filled.Face,
                    contentDescription = "Assignee $i",
                    tint = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.size(avatarSize * 0.7f)
                )
            }
        }

        // Circular "+" button
        Box(
            modifier = Modifier
                .offset(x = overlapOffset * displayCount)
                .size(avatarSize)
                .border(1.5.dp, AppBackgroundDark, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF1E2D44))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onAddClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add assignee",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(avatarSize * 0.6f)
            )
        }
    }
}
