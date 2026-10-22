package com.mightypen.tasc.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mightypen.tasc.model.Task
import com.mightypen.tasc.model.TaskStatus
import com.mightypen.tasc.ui.theme.getCardPalette
import java.util.Locale

@Composable
fun TaskCard(
    task: Task,
    modifier: Modifier = Modifier,
    cardHeight: androidx.compose.ui.unit.Dp = 114.dp,
    onCardClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onToggleComplete: () -> Unit = {},
    onStatusChange: (TaskStatus) -> Unit = {}
) {
    val palette = getCardPalette(task.colorTheme, task.customColorHex)
    var menuExpanded by remember { mutableStateOf(false) }

    val isCompleted = task.status == TaskStatus.COMPLETED

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    // Animated completion progress (0f when running, 1f when completed)
    val completionProgress by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "completionProgress"
    )

    // Guard so shimmer sweep ONLY runs when this specific task is marked complete by user,
    // and never on screen load or when other tasks update.
    var hasCardInitialized by remember { mutableStateOf(false) }
    var wasCardCompleted by remember { mutableStateOf(isCompleted) }

    val shimmerAnim = remember { Animatable(0f) }
    LaunchedEffect(isCompleted) {
        if (!hasCardInitialized) {
            hasCardInitialized = true
            wasCardCompleted = isCompleted
            return@LaunchedEffect
        }
        if (isCompleted && !wasCardCompleted) {
            shimmerAnim.snapTo(0f)
            shimmerAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
        } else {
            shimmerAnim.snapTo(0f)
        }
        wasCardCompleted = isCompleted
    }

    val categoryText = task.subtitle.ifBlank { "General" }
    val tabWidth = (categoryText.length * 8 + 48).coerceIn(104, 150).dp
    val tabHeight = (cardHeight * 0.30f).coerceIn(28.dp, 36.dp)
    val bodyTopPadding = (cardHeight * 0.07f).coerceIn(4.dp, 12.dp)
    val bodyBottomPadding = (cardHeight * 0.14f).coerceIn(8.dp, 20.dp)
    val checkboxSize = (cardHeight * 0.42f).coerceIn(38.dp, 50.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
    ) {
        // 1. Colored notched card background and main body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    NotchedCardShape(
                        tabWidth = tabWidth,
                        tabHeight = tabHeight,
                        cornerRadius = 24.dp,
                        filletRadius = 14.dp
                    )
                )
                .background(palette.background)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onCardClick
                )
        ) {
            // Subtle watermark rosette seal on background when completed
            if (completionProgress > 0.05f) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(end = 24.dp, bottom = 8.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w * 0.88f, h * 0.62f)
                    val sealAlpha = 0.07f * completionProgress

                    drawCircle(
                        color = palette.text.copy(alpha = sealAlpha),
                        radius = 36.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(
                        color = palette.text.copy(alpha = sealAlpha * 0.7f),
                        radius = 28.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            // Luminous Shimmer Wave Sweep
            if (shimmerAnim.value > 0f && shimmerAnim.value < 1f) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val prog = shimmerAnim.value
                    val sweepX = (w + 240.dp.toPx()) * prog - 120.dp.toPx()
                    val beamWidth = 100.dp.toPx()

                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.28f),
                                palette.iconColor.copy(alpha = 0.40f),
                                Color.Transparent
                            ),
                            start = Offset(sweepX - beamWidth, 0f),
                            end = Offset(sweepX + beamWidth, h)
                        )
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Tab Header row (displays Task Category instead of progress)
                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .height(tabHeight)
                        .padding(start = 16.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AnimatedContent(
                        targetState = isCompleted,
                        transitionSpec = {
                            fadeIn(tween(250)) togetherWith fadeOut(tween(200))
                        },
                        label = "tabCategoryAnim"
                    ) { completed ->
                        if (completed) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = palette.text,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = categoryText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Text(
                                text = categoryText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Main card body row (Interactive Checkbox + Title/Subtitle with accomplishment styling + Menu)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 12.dp, top = bodyTopPadding, bottom = bodyBottomPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Interactive Animated Checkbox with celebratory confetti sparkles
                    TaskCheckbox(
                        checked = isCompleted,
                        onCheckedChange = onToggleComplete,
                        accentColor = palette.background,
                        size = checkboxSize
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    // Center: Creative Title & Subtitle (Accomplishment badge + signature underline - NO strikethrough!)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = task.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) palette.text.copy(alpha = 0.72f) else palette.text,
                                maxLines = 1,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            // Accomplishment "Done" badge that smoothly pops in next to title
                            if (completionProgress > 0.05f) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .scale(completionProgress)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(palette.text.copy(alpha = 0.14f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.DoneAll,
                                            contentDescription = null,
                                            tint = palette.text,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Done",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = palette.text
                                        )
                                    }
                                }
                            }
                        }

                        // Creative Signature Underline (Executive approval line that smoothly glides into place)
                        if (completionProgress > 0.02f) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f * completionProgress)
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                palette.text.copy(alpha = 0.80f),
                                                palette.text.copy(alpha = 0.30f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.subtitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isCompleted) palette.subtext.copy(alpha = 0.75f) else palette.subtext,
                            maxLines = 1
                        )
                    }

                    // Right: Three vertical dots action menu
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Task options",
                                tint = palette.text.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Color(0xFF18253A))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isCompleted) "Mark as Incomplete" else "Mark as Completed",
                                        color = Color.White
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = palette.background)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onToggleComplete()
                                }
                            )

                            if (task.status != TaskStatus.RUNNING) {
                                DropdownMenuItem(
                                    text = { Text("Mark as Running", color = Color.White) },
                                    leadingIcon = {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color(0xFF64B5F6))
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onStatusChange(TaskStatus.RUNNING)
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text("Edit Task", color = Color.White) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.LightGray)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEditClick()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Delete Task", color = Color(0xFFFF6E6E)) },
                                leadingIcon = {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = Color(0xFFFF6E6E))
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
