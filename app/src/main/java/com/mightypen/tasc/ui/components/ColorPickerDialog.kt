package com.mightypen.tasc.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt

@Composable
fun ColorPickerDialog(
    initialHex: String = "#FF6B6B",
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Parse initial color to HSV
    val initialHsv = remember(initialHex) {
        val hsv = FloatArray(3)
        try {
            val clean = if (initialHex.startsWith("#")) initialHex else "#$initialHex"
            val colorInt = AndroidColor.parseColor(clean)
            AndroidColor.colorToHSV(colorInt, hsv)
        } catch (e: Exception) {
            hsv[0] = 0f
            hsv[1] = 0.85f
            hsv[2] = 0.95f
        }
        hsv
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    val currentPickedColor = remember(hue, saturation, value) {
        val colorInt = AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
        Color(colorInt)
    }

    val currentHexCode = remember(hue, saturation, value) {
        val colorInt = AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
        String.format("#%06X", 0xFFFFFF and colorInt)
    }

    val isCurrentColorLight = remember(currentPickedColor) {
        (0.299f * currentPickedColor.red + 0.587f * currentPickedColor.green + 0.114f * currentPickedColor.blue) > 0.48f
    }

    // Quick modern palette swatches
    val quickSwatches = listOf(
        "#FF5252", "#FF793F", "#FFB142",
        "#2ED573", "#1DD1A1", "#00D2D3",
        "#54A0FF", "#3867D6", "#5F27CD",
        "#FF6B81", "#E056FD", "#FFFFFF"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF142033)),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1D2E47)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Colorize,
                                contentDescription = null,
                                tint = currentPickedColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Color Picker",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF8FA0B9),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2D Saturation-Value Interactive Canvas
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    val density = LocalDensity.current
                    val widthPx = with(density) { maxWidth.toPx() }
                    val heightPx = with(density) { maxHeight.toPx() }

                    val baseHueColor = remember(hue) {
                        Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f)))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                // 1. Base pure hue background
                                drawRect(color = baseHueColor)
                                // 2. Horizontal gradient: White -> Transparent
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color.White, Color.Transparent)
                                    )
                                )
                                // 3. Vertical gradient: Transparent -> Black
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black)
                                    )
                                )
                            }
                            .pointerInput(widthPx, heightPx) {
                                detectTapGestures { offset ->
                                    saturation = (offset.x / widthPx).coerceIn(0f, 1f)
                                    value = 1f - (offset.y / heightPx).coerceIn(0f, 1f)
                                }
                            }
                            .pointerInput(widthPx, heightPx) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val offset = change.position
                                    saturation = (offset.x / widthPx).coerceIn(0f, 1f)
                                    value = 1f - (offset.y / heightPx).coerceIn(0f, 1f)
                                }
                            }
                    ) {
                        // Selector Thumb Circle on 2D Pad
                        val thumbXPx = saturation * widthPx
                        val thumbYPx = (1f - value) * heightPx
                        val thumbSizeDp = 22.dp
                        val thumbHalfSizePx = with(density) { (thumbSizeDp / 2).toPx() }

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = (thumbXPx - thumbHalfSizePx).roundToInt(),
                                        y = (thumbYPx - thumbHalfSizePx).roundToInt()
                                    )
                                }
                                .size(thumbSizeDp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(currentPickedColor)
                                .border(2.5.dp, Color.White, CircleShape)
                                .border(3.5.dp, Color.Black.copy(alpha = 0.4f), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rainbow Hue Slider Bar
                Text(
                    text = "Hue Spectrum",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8FA0B9)
                )
                Spacer(modifier = Modifier.height(6.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .clip(RoundedCornerShape(13.dp))
                ) {
                    val density = LocalDensity.current
                    val widthPx = with(density) { maxWidth.toPx() }

                    val rainbowBrush = remember {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFF0000),
                                Color(0xFFFFFF00),
                                Color(0xFF00FF00),
                                Color(0xFF00FFFF),
                                Color(0xFF0000FF),
                                Color(0xFFFF00FF),
                                Color(0xFFFF0000)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(rainbowBrush)
                            .pointerInput(widthPx) {
                                detectTapGestures { offset ->
                                    hue = (offset.x / widthPx).coerceIn(0f, 1f) * 360f
                                }
                            }
                            .pointerInput(widthPx) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    hue = (change.position.x / widthPx).coerceIn(0f, 1f) * 360f
                                }
                            }
                    ) {
                        // Hue Draggable Thumb
                        val thumbX = (hue / 360f) * widthPx
                        val thumbHalfPx = with(density) { 13.dp.toPx() }

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = (thumbX - thumbHalfPx).roundToInt().coerceIn(0, (widthPx - thumbHalfPx * 2).roundToInt()),
                                        y = 0
                                    )
                                }
                                .size(26.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))))
                                .border(2.5.dp, Color.White, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Live Color Preview & Hex Display Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF19263C))
                        .border(1.dp, Color(0xFF283B55), RoundedCornerShape(14.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Swatch Box
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(4.dp, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(currentPickedColor)
                                .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Selected Hex",
                                fontSize = 11.sp,
                                color = Color(0xFF7E92AC),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currentHexCode,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }
                    }

                    // Reset to initial color button (if different)
                    if (currentHexCode != initialHex.uppercase()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF233651))
                                .clickable {
                                    hue = initialHsv[0]
                                    saturation = initialHsv[1]
                                    value = initialHsv[2]
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Reset",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD6F58F)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Palette Inspiration Row
                Text(
                    text = "Quick Presets:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7E92AC)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    quickSwatches.forEach { hex ->
                        val swatchCol = try {
                            Color(AndroidColor.parseColor(hex))
                        } catch (e: Exception) {
                            Color.Gray
                        }
                        val isSelected = currentHexCode.equals(hex, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(swatchCol)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.35f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    val hsv = FloatArray(3)
                                    AndroidColor.colorToHSV(AndroidColor.parseColor(hex), hsv)
                                    hue = hsv[0]
                                    saturation = hsv[1]
                                    value = hsv[2]
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Cancel and Select Color
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF8FA0B9)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF283B55))
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            onColorSelected(currentHexCode)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentPickedColor,
                            contentColor = if (isCurrentColorLight) Color(0xFF101B24) else Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Select Color",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
