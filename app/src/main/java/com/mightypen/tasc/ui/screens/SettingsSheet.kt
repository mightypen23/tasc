package com.mightypen.tasc.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mightypen.tasc.data.TaskCardSizePreset
import com.mightypen.tasc.model.Task
import com.mightypen.tasc.model.TaskColorTheme
import com.mightypen.tasc.model.TaskStatus
import com.mightypen.tasc.ui.components.ColorPickerDialog
import com.mightypen.tasc.ui.components.TaskCard
import com.mightypen.tasc.ui.theme.AppThemes
import com.mightypen.tasc.ui.theme.SelectedDatePill
import com.mightypen.tasc.ui.theme.SelectedDateText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    cardSizePreset: TaskCardSizePreset,
    cardHeightDp: Int,
    currentThemeId: String,
    customColors: List<String>,
    onSaveSettings: (TaskCardSizePreset, Int, String) -> Unit,
    onAddCustomColor: (String) -> Boolean,
    onRemoveCustomColor: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Draft local states - only persisted upon clicking "Save Settings"
    var draftPreset by remember(cardSizePreset) { mutableStateOf(cardSizePreset) }
    var draftHeightDp by remember(cardHeightDp) { mutableFloatStateOf(cardHeightDp.toFloat()) }
    var draftThemeId by remember(currentThemeId) { mutableStateOf(currentThemeId) }
    val draftTheme = remember(draftThemeId) { AppThemes.fromId(draftThemeId) }

    var newHexInput by remember { mutableStateOf("") }
    var showColorPicker by remember { mutableStateOf(false) }

    // Curated quick inspiration colors for task folders
    val quickInspiration = listOf(
        "#FF6B6B", // Sunset Coral
        "#4ECDC4", // Teal Blue
        "#A8E6CF", // Fresh Mint
        "#FD79A8", // Flamingo Pink
        "#A29BFE", // Royal Periwinkle
        "#FFA502", // Warm Amber
        "#00CEC9", // Electric Robin
        "#6C5CE7"  // Deep Violet
    )

    // Live preview color for custom hex input
    val parsedInputColor: Color? = remember(newHexInput) {
        val clean = if (newHexInput.startsWith("#")) newHexInput else "#$newHexInput"
        if (clean.matches(Regex("^#([A-F0-9]{6}|[A-F0-9]{8})$", RegexOption.IGNORE_CASE))) {
            try {
                Color(android.graphics.Color.parseColor(clean))
            } catch (e: Exception) {
                null
            }
        } else null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF142033),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF334664))
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pinned Sticky Header with Title and Top Corner Save Action Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 20.dp, top = 2.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Corner "Save" Button (Instant access without scrolling to bottom!)
                    Button(
                        onClick = {
                            onSaveSettings(draftPreset, draftHeightDp.toInt(), draftThemeId)
                            Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = draftTheme.accentButton,
                            contentColor = draftTheme.onAccentButton
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Save",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Scrollable Settings Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
                    .verticalScroll(rememberScrollState())
            ) {

            // ==========================================
            // SECTION 1: Task Folders Vertical Size
            // ==========================================
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Height,
                    contentDescription = null,
                    tint = draftTheme.accentButton,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Task Folder Vertical Size",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Adjust the vertical height and compactness of your task folders.",
                fontSize = 13.sp,
                color = Color(0xFF8FA0B9)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Preset Buttons: Compact, Standard, Spacious
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    TaskCardSizePreset.COMPACT,
                    TaskCardSizePreset.STANDARD,
                    TaskCardSizePreset.SPACIOUS
                ).forEach { preset ->
                    val isSelected = draftPreset == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) draftTheme.accentButton else Color(0xFF1E2C42))
                            .clickable {
                                draftPreset = preset
                                draftHeightDp = preset.heightDp.toFloat()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = preset.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) draftTheme.onAccentButton else Color.White
                            )
                            Text(
                                text = "${preset.heightDp} dp",
                                fontSize = 10.sp,
                                color = if (isSelected) draftTheme.onAccentButton.copy(alpha = 0.75f) else Color(0xFF7E92AC)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Height Slider with Live Value Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Height",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8FA0B9)
                )
                Text(
                    text = "${draftHeightDp.toInt()} dp",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = draftTheme.accentButton
                )
            }

            Slider(
                value = draftHeightDp,
                onValueChange = {
                    draftHeightDp = it
                    draftPreset = TaskCardSizePreset.fromHeight(it.toInt())
                },
                valueRange = 84f..150f,
                colors = SliderDefaults.colors(
                    thumbColor = draftTheme.accentButton,
                    activeTrackColor = draftTheme.accentButton,
                    inactiveTrackColor = Color(0xFF23354E)
                )
            )

            // Live Miniature Preview of Task Folder
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Live Folder Preview:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6F87A7)
            )
            Spacer(modifier = Modifier.height(6.dp))

            TaskCard(
                task = Task(
                    title = "Design System Sync",
                    subtitle = "Client project folder",
                    startTime = "10 am",
                    endTime = "12 pm",
                    status = TaskStatus.RUNNING,
                    progress = 68,
                    colorTheme = TaskColorTheme.LIME
                ),
                cardHeight = draftHeightDp.toInt().dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ==========================================
            // SECTION 2: App Color Theme (Button & Background)
            // ==========================================
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ColorLens,
                    contentDescription = null,
                    tint = draftTheme.accentButton,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Color Theme",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose curated button & background color pairs for your app.",
                fontSize = 13.sp,
                color = Color(0xFF8FA0B9)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2-column Grid of Curated Theme Pairs
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AppThemes.allThemes.chunked(2).forEach { themePairList ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        themePairList.forEach { theme ->
                            val isSelected = draftThemeId.equals(theme.id, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(theme.backgroundTop, theme.backgroundDark, theme.backgroundBottom)
                                        )
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) theme.accentButton else Color(0xFF283B55),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        draftThemeId = theme.id
                                    }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = theme.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.accentButton),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = theme.onAccentButton,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Preview of Button on this background
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.25f))
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(theme.accentButton),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = null,
                                                tint = theme.onAccentButton,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Button Accent",
                                            fontSize = 11.sp,
                                            color = theme.accentButton,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ==========================================
            // SECTION 3: Custom Task Color Codes
            // ==========================================
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.FormatPaint,
                    contentDescription = null,
                    tint = draftTheme.accentButton,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Custom Task Color Codes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add custom hex color codes to use when creating or editing tasks.",
                fontSize = 13.sp,
                color = Color(0xFF8FA0B9)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Active Custom Colors List
            if (customColors.isNotEmpty()) {
                Text(
                    text = "Saved Custom Colors (${customColors.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7E92AC)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    customColors.chunked(2).forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowColors.forEach { hex ->
                                val swatchColor = try {
                                    Color(android.graphics.Color.parseColor(hex))
                                } catch (e: Exception) {
                                    Color.Gray
                                }

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF19263C))
                                        .border(1.dp, Color(0xFF283B55), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(swatchColor)
                                                .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = hex,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.White
                                        )
                                    }

                                    IconButton(
                                        onClick = { onRemoveCustomColor(hex) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "Remove color",
                                            tint = Color(0xFFFF6E6E),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            if (rowColors.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Add Custom Color Input Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF19263C))
                    .border(1.dp, Color(0xFF283B55), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Add New Color Code",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Live Color Preview Rectangle (Click to open Color Picker!)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(parsedInputColor ?: Color(0xFF22354E))
                                    .border(
                                        width = 1.8.dp,
                                        color = if (parsedInputColor != null) Color.White else draftTheme.accentButton.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        showColorPicker = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val isLight = parsedInputColor?.let {
                                    (0.299f * it.red + 0.587f * it.green + 0.114f * it.blue) > 0.48f
                                } ?: false

                                Icon(
                                    imageVector = Icons.Filled.Colorize,
                                    contentDescription = "Open Color Picker",
                                    tint = if (parsedInputColor == null) draftTheme.accentButton else (if (isLight) Color(0xFF101B24) else Color.White),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Pick Color",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = draftTheme.accentButton
                            )
                        }

                        // Hex Input Field
                        OutlinedTextField(
                            value = newHexInput,
                            onValueChange = { input ->
                                val sanitized = input.replace("#", "").take(6).uppercase()
                                newHexInput = if (sanitized.isNotEmpty()) "#$sanitized" else ""
                            },
                            placeholder = { Text("#RRGGBB", color = Color(0xFF566982), fontFamily = FontFamily.Monospace) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (parsedInputColor != null) {
                                        val added = onAddCustomColor(newHexInput)
                                        if (added) {
                                            Toast.makeText(context, "Added $newHexInput", Toast.LENGTH_SHORT).show()
                                            newHexInput = ""
                                        }
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = draftTheme.accentButton,
                                unfocusedBorderColor = Color(0xFF283B55),
                                focusedContainerColor = Color(0xFF142033),
                                unfocusedContainerColor = Color(0xFF142033)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Add Button
                        Button(
                            onClick = {
                                if (parsedInputColor != null) {
                                    val added = onAddCustomColor(newHexInput)
                                    if (added) {
                                        Toast.makeText(context, "Added $newHexInput", Toast.LENGTH_SHORT).show()
                                        newHexInput = ""
                                    } else {
                                        Toast.makeText(context, "Color already exists", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = parsedInputColor != null,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = draftTheme.accentButton,
                                contentColor = draftTheme.onAccentButton,
                                disabledContainerColor = Color(0xFF22354E),
                                disabledContentColor = Color(0xFF566982)
                            ),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Palette Inspiration Row
                    Text(
                        text = "Quick Palette Inspiration (Tap to load):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF7E92AC)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        quickInspiration.forEach { hex ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                Color.Gray
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                    .clickable {
                                        newHexInput = hex
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ==========================================
            // EXPLICIT SAVE SETTINGS ACTION BUTTON
            // ==========================================
            Button(
                onClick = {
                    onSaveSettings(draftPreset, draftHeightDp.toInt(), draftThemeId)
                    Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = draftTheme.accentButton,
                    contentColor = draftTheme.onAccentButton
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

        // Color Picker Dialog (triggered when user clicks the color preview rectangle)
        if (showColorPicker) {
            ColorPickerDialog(
                initialHex = if (parsedInputColor != null) newHexInput else "#4ECDC4",
                onColorSelected = { pickedHex ->
                    newHexInput = pickedHex
                },
                onDismiss = { showColorPicker = false }
            )
        }
    }
}
