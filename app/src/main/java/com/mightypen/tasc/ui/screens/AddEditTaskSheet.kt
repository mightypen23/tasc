package com.mightypen.tasc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mightypen.tasc.model.Task
import com.mightypen.tasc.model.TaskColorTheme
import com.mightypen.tasc.model.TaskStatus
import com.mightypen.tasc.ui.components.ColorPickerDialog
import com.mightypen.tasc.ui.components.TaskCard
import com.mightypen.tasc.ui.components.TimePickerDialog
import com.mightypen.tasc.ui.theme.CardCyanBg
import com.mightypen.tasc.ui.theme.CardLavenderBg
import com.mightypen.tasc.ui.theme.CardLimeBg
import com.mightypen.tasc.ui.theme.CardYellowBg
import com.mightypen.tasc.ui.theme.SelectedDatePill
import com.mightypen.tasc.ui.theme.SelectedDateText
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskSheet(
    taskToEdit: Task?,
    currentSelectedDate: LocalDate,
    customColors: List<String> = emptyList(),
    categories: List<String> = emptyList(),
    onAddCategory: (String) -> Unit = {},
    onRemoveCategory: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onSaveTask: (Task) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var subtitle by remember { mutableStateOf(taskToEdit?.subtitle ?: "") }
    var startTime by remember { mutableStateOf(taskToEdit?.startTime ?: "10 am") }
    var endTime by remember { mutableStateOf(taskToEdit?.endTime ?: "02 pm") }
    var selectedStatus by remember { mutableStateOf(taskToEdit?.status ?: TaskStatus.RUNNING) }
    var selectedTheme by remember { mutableStateOf(taskToEdit?.colorTheme ?: TaskColorTheme.LIME) }
    var selectedCustomHex by remember { mutableStateOf<String?>(taskToEdit?.customColorHex) }
    var progress by remember { mutableFloatStateOf((taskToEdit?.progress ?: 0).toFloat()) }
    var taskDateStr by remember { mutableStateOf(taskToEdit?.date ?: currentSelectedDate.toString()) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }
    var reminderMinutesBefore by remember { mutableStateOf<Int?>(taskToEdit?.reminderMinutesBefore ?: 0) }
    var showCustomReminderDialog by remember { mutableStateOf(false) }
    var customReminderInput by remember { mutableStateOf("") }

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (taskToEdit == null) "Create New Task" else "Edit Task",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Title Field
            Text("Task Title", color = Color(0xFF8FA0B9), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("e.g. Web Design", color = Color(0xFF566982)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SelectedDatePill,
                    unfocusedBorderColor = Color(0xFF283B55),
                    focusedContainerColor = Color(0xFF19263C),
                    unfocusedContainerColor = Color(0xFF19263C)
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Folder Category Field
            Text("Folder Category", color = Color(0xFF8FA0B9), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                placeholder = { Text("e.g. Work, Personal, Design", color = Color(0xFF566982)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SelectedDatePill,
                    unfocusedBorderColor = Color(0xFF283B55),
                    focusedContainerColor = Color(0xFF19263C),
                    unfocusedContainerColor = Color(0xFF19263C)
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Category Suggestion Chips with Add (+) and Remove (X)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "+" Button to add additional custom category
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, end = 2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SelectedDatePill.copy(alpha = 0.16f))
                        .border(1.dp, SelectedDatePill.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                        .clickable { showAddCategoryDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Category",
                            tint = SelectedDatePill,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Add",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelectedDatePill
                        )
                    }
                }

                val activeCategories = categories.ifEmpty {
                    listOf("Work", "Design", "Personal", "Health", "Study", "Meeting")
                }

                activeCategories.forEach { cat ->
                    val isSelected = subtitle.equals(cat, ignoreCase = true)
                    Box(
                        modifier = Modifier.padding(top = 6.dp, end = 6.dp)
                    ) {
                        // Category Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SelectedDatePill.copy(alpha = 0.22f) else Color(0xFF19263C))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) SelectedDatePill else Color(0xFF2B3D55),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { subtitle = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) SelectedDatePill else Color(0xFF8FA0B9)
                            )
                        }

                        // "X" button at the top corner of each category
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF24364D))
                                .border(1.dp, Color(0xFF142033), CircleShape)
                                .clickable {
                                    onRemoveCategory(cat)
                                    if (subtitle.equals(cat, ignoreCase = true)) {
                                        subtitle = ""
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove $cat category",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Slots (Interactive Clock Selectors matching Screenshot 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Time Card
                Column(modifier = Modifier.weight(1f)) {
                    Text("Start Time", color = Color(0xFF8FA0B9), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF19263C))
                            .border(1.dp, Color(0xFF283B55), RoundedCornerShape(14.dp))
                            .clickable { showStartTimePicker = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = startTime,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = "Pick start time",
                                tint = SelectedDatePill,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // End Time Card
                Column(modifier = Modifier.weight(1f)) {
                    Text("End Time", color = Color(0xFF8FA0B9), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF19263C))
                            .border(1.dp, Color(0xFF283B55), RoundedCornerShape(14.dp))
                            .clickable { showEndTimePicker = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = endTime,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = "Pick end time",
                                tint = SelectedDatePill,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // SCHEDULED NOTIFICATION TIMING SELECTOR
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (reminderMinutesBefore != null) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsNone,
                        contentDescription = null,
                        tint = if (reminderMinutesBefore != null) SelectedDatePill else Color(0xFF8FA0B9),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Notification Reminder",
                        color = Color(0xFF8FA0B9),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = when (reminderMinutesBefore) {
                        null -> "None"
                        0 -> "On time"
                        else -> "${reminderMinutesBefore}m before"
                    },
                    color = if (reminderMinutesBefore != null) SelectedDatePill else Color(0xFF566982),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reminder Quick Options
            val reminderOptions = listOf(
                null to "None",
                0 to "On time",
                5 to "5m",
                10 to "10m",
                15 to "15m",
                30 to "30m"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                reminderOptions.forEach { (mins, label) ->
                    val isSelected = reminderMinutesBefore == mins
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) SelectedDatePill else Color(0xFF19263C))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) SelectedDatePill else Color(0xFF283B55),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { reminderMinutesBefore = mins }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SelectedDateText else Color(0xFFBAC7D5)
                        )
                    }
                }

                // Custom timing option
                val isCustom = reminderMinutesBefore != null && reminderMinutesBefore !in listOf(0, 5, 10, 15, 30)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCustom) SelectedDatePill else Color(0xFF19263C))
                        .border(
                            width = 1.dp,
                            color = if (isCustom) SelectedDatePill else Color(0xFF283B55),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            customReminderInput = if (isCustom) "${reminderMinutesBefore}" else ""
                            showCustomReminderDialog = true
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = if (isCustom) "${reminderMinutesBefore}m (Custom)" else "Custom...",
                        fontSize = 12.sp,
                        fontWeight = if (isCustom) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCustom) SelectedDateText else Color(0xFFBAC7D5)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            val timingDescription = when (reminderMinutesBefore) {
                null -> "🔕 No notification will be sent."
                0 -> "🔔 Folder notification will alert on time when task starts ($startTime)."
                else -> "🔔 Folder notification will alert $reminderMinutesBefore minutes before ($startTime)."
            }
            Text(
                text = timingDescription,
                fontSize = 11.sp,
                color = if (reminderMinutesBefore != null) SelectedDatePill.copy(alpha = 0.85f) else Color(0xFF566982)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status Selector Pills
            Text("Status", color = Color(0xFF8FA0B9), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskStatus.entries.forEach { status ->
                    val isSelected = selectedStatus == status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SelectedDatePill else Color(0xFF1E2C42))
                            .clickable {
                                selectedStatus = status
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = status.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SelectedDateText else Color(0xFF8FA0B9)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Theme Selector
            Text("Preset Colors", color = Color(0xFF8FA0B9), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            // Presets Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val themeColors = listOf(
                    TaskColorTheme.LIME to CardLimeBg,
                    TaskColorTheme.CYAN to CardCyanBg,
                    TaskColorTheme.LAVENDER to CardLavenderBg,
                    TaskColorTheme.YELLOW to CardYellowBg
                )

                themeColors.forEach { (theme, color) ->
                    val isSelected = selectedCustomHex == null && selectedTheme == theme
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                selectedTheme = theme
                                selectedCustomHex = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFF101B24),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Custom Colors Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Colors (${customColors.size})",
                    color = Color(0xFF8FA0B9),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (customColors.isNotEmpty()) {
                    Text(
                        text = "Scroll to view all ➔",
                        color = Color(0xFF566982),
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Eyedropper / Color Picker Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showColorPicker = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1D2F47))
                            .border(
                                width = 1.5.dp,
                                color = SelectedDatePill.copy(alpha = 0.8f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Colorize,
                            contentDescription = "Pick Custom Color",
                            tint = SelectedDatePill,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Picker",
                        fontSize = 10.sp,
                        color = SelectedDatePill,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Custom User-Saved Colors
                customColors.forEach { hex ->
                    val isSelected = selectedCustomHex == hex
                    val parsedColor = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (e: Exception) {
                        Color.Gray
                    }
                    val isLight = (0.299f * parsedColor.red + 0.587f * parsedColor.green + 0.114f * parsedColor.blue) > 0.46f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            selectedCustomHex = hex
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.35f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = if (isLight) Color(0xFF101B24) else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hex,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSelected) Color.White else Color(0xFF7E92AC),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Live Preview of the folder
            Text("Folder Preview", color = Color(0xFF8FA0B9), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            TaskCard(
                task = Task(
                    title = title.ifBlank { "Task Title" },
                    subtitle = subtitle.ifBlank { "Task category or subtitle" },
                    startTime = startTime,
                    endTime = endTime,
                    status = selectedStatus,
                    progress = progress.toInt(),
                    colorTheme = selectedTheme,
                    customColorHex = selectedCustomHex
                ),
                cardHeight = 110.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Action Button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val task = Task(
                            id = taskToEdit?.id ?: 0,
                            title = title.trim(),
                            subtitle = subtitle.trim().ifEmpty { "General task" },
                            startTime = startTime.trim(),
                            endTime = endTime.trim(),
                            status = selectedStatus,
                            progress = progress.toInt(),
                            colorTheme = selectedTheme,
                            customColorHex = selectedCustomHex,
                            date = taskDateStr,
                            reminderMinutesBefore = reminderMinutesBefore,
                            createdAt = taskToEdit?.createdAt ?: System.currentTimeMillis()
                        )
                        onSaveTask(task)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SelectedDatePill,
                    contentColor = SelectedDateText
                )
            ) {
                Text(
                    text = if (taskToEdit == null) "Create Task" else "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showColorPicker) {
            ColorPickerDialog(
                initialHex = selectedCustomHex ?: "#4ECDC4",
                onColorSelected = { pickedHex ->
                    selectedCustomHex = pickedHex
                },
                onDismiss = { showColorPicker = false }
            )
        }

        if (showStartTimePicker) {
            TimePickerDialog(
                initialTime = startTime,
                title = "SESSION START TIME",
                onTimeSelected = { pickedTime ->
                    startTime = pickedTime
                    showStartTimePicker = false
                },
                onDismiss = { showStartTimePicker = false }
            )
        }

        if (showEndTimePicker) {
            TimePickerDialog(
                initialTime = endTime,
                title = "SESSION END TIME",
                onTimeSelected = { pickedTime ->
                    endTime = pickedTime
                    showEndTimePicker = false
                },
                onDismiss = { showEndTimePicker = false }
            )
        }

        if (showAddCategoryDialog) {
            Dialog(onDismissRequest = {
                showAddCategoryDialog = false
                newCategoryText = ""
            }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF142033)),
                    border = BorderStroke(1.5.dp, Color(0xFF283B55))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Add Custom Category",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = newCategoryText,
                            onValueChange = { newCategoryText = it },
                            placeholder = { Text("e.g. Finance, Fitness, Study", color = Color(0xFF566982)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SelectedDatePill,
                                unfocusedBorderColor = Color(0xFF283B55),
                                focusedContainerColor = Color(0xFF19263C),
                                unfocusedContainerColor = Color(0xFF19263C)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                showAddCategoryDialog = false
                                newCategoryText = ""
                            }) {
                                Text(
                                    text = "CANCEL",
                                    color = Color(0xFF8FA0B9),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    val trimmed = newCategoryText.trim()
                                    if (trimmed.isNotBlank()) {
                                        onAddCategory(trimmed)
                                        subtitle = trimmed
                                        showAddCategoryDialog = false
                                        newCategoryText = ""
                                    }
                                }
                            ) {
                                Text(
                                    text = "ADD",
                                    color = SelectedDatePill,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showCustomReminderDialog) {
            Dialog(onDismissRequest = {
                showCustomReminderDialog = false
                customReminderInput = ""
            }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF142033)),
                    border = BorderStroke(1.5.dp, Color(0xFF283B55))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Custom Reminder Timing",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enter how many minutes before task start to be notified (e.g. 20, 45, 60):",
                            fontSize = 12.sp,
                            color = Color(0xFF8FA0B9)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = customReminderInput,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 4) {
                                    customReminderInput = input
                                }
                            },
                            placeholder = { Text("e.g. 45", color = Color(0xFF566982)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SelectedDatePill,
                                unfocusedBorderColor = Color(0xFF283B55),
                                focusedContainerColor = Color(0xFF19263C),
                                unfocusedContainerColor = Color(0xFF19263C)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                showCustomReminderDialog = false
                                customReminderInput = ""
                            }) {
                                Text(
                                    text = "CANCEL",
                                    color = Color(0xFF8FA0B9),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    val mins = customReminderInput.toIntOrNull()
                                    if (mins != null && mins > 0) {
                                        reminderMinutesBefore = mins
                                        showCustomReminderDialog = false
                                        customReminderInput = ""
                                    }
                                }
                            ) {
                                Text(
                                    text = "SET",
                                    color = SelectedDatePill,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
