package com.mightypen.tasc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mightypen.tasc.ui.theme.SelectedDatePill
import com.mightypen.tasc.ui.theme.SelectedDateText
import java.util.Locale

@Composable
fun RetroCard(
    backgroundColor: Color,
    borderColor: Color,
    shadowColor: Color,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.5.dp,
    shadowOffset: Dp = 4.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(modifier = modifier) {
        // Offset shadow behind
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .clip(shape)
                .background(shadowColor)
        )
        // Main card body with border
        Box(
            modifier = Modifier
                .clip(shape)
                .background(backgroundColor)
                .border(borderWidth, borderColor, shape)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetroTimePicker(
    initialHour: Int = 10,
    initialMinute: Int = 0,
    is24Hour: Boolean = false,
    titleText: String = "SELECT TIME",
    accentColor: Color = SelectedDatePill,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        RetroCard(
            backgroundColor = Color(0xFF142033),
            borderColor = Color(0xFF283B55),
            shadowColor = Color(0xFF090E16),
            cornerRadius = 20.dp,
            borderWidth = 1.5.dp,
            shadowOffset = 4.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = titleText,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFF8FA0B9),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier.scale(0.90f),
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color(0xFF18253A),
                            clockDialSelectedContentColor = SelectedDateText,
                            clockDialUnselectedContentColor = Color(0xFFDCE6F2),
                            selectorColor = accentColor,
                            containerColor = Color(0xFF142033),
                            periodSelectorBorderColor = Color(0xFF2A3D58),
                            periodSelectorSelectedContainerColor = accentColor,
                            periodSelectorUnselectedContainerColor = Color(0xFF19263C),
                            periodSelectorSelectedContentColor = SelectedDateText,
                            periodSelectorUnselectedContentColor = Color(0xFF8FA0B9),
                            timeSelectorSelectedContainerColor = accentColor.copy(alpha = 0.22f),
                            timeSelectorUnselectedContainerColor = Color(0xFF19263C),
                            timeSelectorSelectedContentColor = accentColor,
                            timeSelectorUnselectedContentColor = Color.White
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "CANCEL",
                            color = Color(0xFF8FA0B9),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(
                        onClick = {
                            onTimeSelected(timePickerState.hour, timePickerState.minute)
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = "CONFIRM",
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Convenient wrapper compatible with existing callers that pass time as a String (e.g. "10:00 am")
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: String = "10:00 am",
    title: String = "SELECT TIME",
    accentColor: Color = SelectedDatePill,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val (parsedHour, parsedMinute) = remember(initialTime) {
        var hour = 10
        var minute = 0
        try {
            val lower = initialTime.trim().lowercase()
            val isPm = lower.contains("pm")
            val digitsOnly = lower.replace("am", "").replace("pm", "").trim()
            if (digitsOnly.contains(":")) {
                val parts = digitsOnly.split(":")
                var h = parts[0].trim().toIntOrNull()?.coerceIn(1, 12) ?: 10
                minute = parts[1].trim().toIntOrNull()?.coerceIn(0, 59) ?: 0
                if (isPm && h < 12) h += 12
                if (!isPm && h == 12) h = 0
                hour = h
            } else {
                var h = digitsOnly.toIntOrNull()?.coerceIn(1, 12) ?: 10
                if (isPm && h < 12) h += 12
                if (!isPm && h == 12) h = 0
                hour = h
                minute = 0
            }
        } catch (_: Exception) {
            hour = 10
            minute = 0
        }
        Pair(hour, minute)
    }

    RetroTimePicker(
        initialHour = parsedHour,
        initialMinute = parsedMinute,
        is24Hour = false,
        titleText = title,
        accentColor = accentColor,
        onTimeSelected = { hour, minute ->
            val period = if (hour >= 12) "pm" else "am"
            val displayHour = when (val h = hour % 12) {
                0 -> 12
                else -> h
            }
            val formatted = String.format(Locale.US, "%02d:%02d %s", displayHour, minute, period)
            onTimeSelected(formatted)
        },
        onDismiss = onDismiss
    )
}
