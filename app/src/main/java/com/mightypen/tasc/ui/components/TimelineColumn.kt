package com.mightypen.tasc.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mightypen.tasc.NotificationHelper
import com.mightypen.tasc.model.Task
import com.mightypen.tasc.model.TaskStatus
import com.mightypen.tasc.ui.theme.TimelineDashedLine
import com.mightypen.tasc.ui.theme.TimelineTimeText
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Task overload for TimelineColumn that extracts times, dates, status and applies active color.
 */
@Composable
fun TimelineColumn(
    task: Task,
    activeColor: Color,
    height: Dp = 130.dp,
    modifier: Modifier = Modifier
) {
    TimelineColumn(
        startTime = task.startTime,
        endTime = task.endTime,
        date = task.date,
        isCompleted = task.status == TaskStatus.COMPLETED,
        isRejected = task.status == TaskStatus.REJECTED,
        activeColor = activeColor,
        height = height,
        modifier = modifier
    )
}

/**
 * Real-time timeline column showing start and end times, with a vertical dashed connector
 * that dynamically fills with a solid line and horizontal crossbar tick mark as time elapses.
 */
@Composable
fun TimelineColumn(
    startTime: String,
    endTime: String,
    date: String? = null,
    isCompleted: Boolean = false,
    isRejected: Boolean = false,
    activeColor: Color = TimelineDashedLine,
    height: Dp = 130.dp,
    modifier: Modifier = Modifier
) {
    // Live 1-second ticker so timeline progress bar smoothly counts elapsed time in real-time
    val currentTime by produceState(initialValue = LocalDateTime.now()) {
        while (true) {
            delay(1000L)
            value = LocalDateTime.now()
        }
    }

    val (targetProgress, isCurrentlyActive) = remember(
        startTime,
        endTime,
        date,
        isCompleted,
        isRejected,
        currentTime
    ) {
        if (isRejected) {
            return@remember 0f to false
        }
        if (isCompleted) {
            return@remember 1f to false
        }

        val parsedStart = parseSafeDateTime(date, startTime)
        val parsedEnd = parseSafeDateTime(date, endTime)

        if (parsedStart == null || parsedEnd == null) {
            return@remember 0f to false
        }

        val actualEnd = if (parsedEnd.isBefore(parsedStart)) {
            parsedEnd.plusDays(1)
        } else {
            parsedEnd
        }

        if (currentTime.isBefore(parsedStart)) {
            0f to false
        } else if (!currentTime.isBefore(actualEnd)) {
            1f to false
        } else {
            val totalSeconds = ChronoUnit.SECONDS.between(parsedStart, actualEnd).coerceAtLeast(1)
            val elapsedSeconds = ChronoUnit.SECONDS.between(parsedStart, currentTime).coerceIn(0, totalSeconds)
            val prog = (elapsedSeconds.toDouble() / totalSeconds.toDouble()).toFloat().coerceIn(0f, 1f)
            prog to true
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 350, easing = LinearEasing),
        label = "timelineProgress"
    )

    Column(
        modifier = modifier
            .width(62.dp)
            .height(height),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Start time
        Text(
            text = startTime,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TimelineTimeText,
            maxLines = 1
        )

        // Vertical timeline connector with live elapsed time counting
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val startY = 8.dp.toPx()
            val endY = size.height - 8.dp.toPx()
            if (endY <= startY) return@Canvas

            val centerX = size.width / 2f
            val totalHeight = endY - startY
            val currentY = (startY + totalHeight * animatedProgress).coerceIn(startY, endY)

            // 1. Background dashed line
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            drawLine(
                color = TimelineDashedLine,
                start = Offset(centerX, startY),
                end = Offset(centerX, endY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = dashEffect
            )

            // 2. Active solid progress bar filling from start down to current time
            if (animatedProgress > 0.005f) {
                drawLine(
                    color = activeColor,
                    start = Offset(centerX, startY),
                    end = Offset(centerX, currentY),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 3. Horizontal crossbar tick mark at the current time position
            if (isCurrentlyActive && animatedProgress in 0.01f..0.99f) {
                val tickHalfWidth = 8.dp.toPx()
                drawLine(
                    color = activeColor,
                    start = Offset(centerX - tickHalfWidth, currentY),
                    end = Offset(centerX + tickHalfWidth, currentY),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // End time
        Text(
            text = endTime,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TimelineTimeText,
            maxLines = 1
        )
    }
}

private fun parseSafeDateTime(dateStr: String?, timeStr: String): LocalDateTime? {
    if (timeStr.isBlank()) return null
    val safeDate = if (!dateStr.isNullOrBlank()) {
        try {
            LocalDate.parse(dateStr.trim())
        } catch (_: Exception) {
            LocalDate.now()
        }
    } else {
        LocalDate.now()
    }
    return NotificationHelper.parseTaskDateTime(safeDate.toString(), timeStr)
}

@Composable
fun TimelineHorizontalDivider(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .height(1.dp)
    ) {
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
        drawLine(
            color = TimelineDashedLine.copy(alpha = 0.5f),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashEffect
        )
    }
}
