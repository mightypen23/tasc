package com.mightypen.tasc.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mightypen.tasc.model.Task
import com.mightypen.tasc.ui.TaskViewModel
import com.mightypen.tasc.ui.components.DateSelectorStrip
import com.mightypen.tasc.ui.components.TaskCard
import com.mightypen.tasc.ui.components.TimelineColumn
import com.mightypen.tasc.ui.components.TimelineHorizontalDivider
import com.mightypen.tasc.ui.components.TopBarHeader
import com.mightypen.tasc.ui.theme.AppBackgroundBottom
import com.mightypen.tasc.ui.theme.AppBackgroundDark
import com.mightypen.tasc.ui.theme.AppBackgroundTop
import com.mightypen.tasc.ui.theme.SelectedDatePill
import com.mightypen.tasc.ui.theme.SelectedDateText
import com.mightypen.tasc.ui.theme.getCardPalette
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val dayTasksState by viewModel.dayTasksState.collectAsState()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsState()
    val monthLabel by viewModel.monthLabel.collectAsState()
    val isAddEditOpen by viewModel.isAddEditOpen.collectAsState()
    val taskToEdit by viewModel.taskToEdit.collectAsState()
    val showMonthYearPicker by viewModel.showMonthYearPicker.collectAsState()
    val cardSizePreset by viewModel.cardSizePreset.collectAsState()
    val cardHeightDp by viewModel.cardHeightDp.collectAsState()
    val customColors by viewModel.customColors.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val appThemeId by viewModel.appThemeId.collectAsState()
    val currentAppTheme by viewModel.currentAppTheme.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val showUpdates by viewModel.showUpdates.collectAsState()

    val currentViewModel by rememberUpdatedState(viewModel)
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedDragOffset by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dragOffsetAnimation"
    )

    val velocityTracker = remember { VelocityTracker() }
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 52.dp.toPx() }
    val velocityThreshold = with(density) { 380.dp.toPx() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = currentAppTheme.backgroundDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateTask() },
                containerColor = currentAppTheme.accentButton,
                contentColor = currentAppTheme.onAccentButton,
                shape = CircleShape,
                modifier = Modifier
                    .shadow(12.dp, CircleShape, spotColor = currentAppTheme.accentButton)
                    .size(58.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            currentAppTheme.backgroundTop,
                            currentAppTheme.backgroundDark,
                            currentAppTheme.backgroundBottom
                        )
                    )
                )
        ) {
            // Header: Today button + "My Tasks" + Month Selector + Dropdown Menu (Settings, Updates, New Task)
            TopBarHeader(
                monthLabel = monthLabel,
                onMonthClick = { viewModel.toggleMonthYearPicker() },
                isToday = dayTasksState.date == LocalDate.now(),
                onTodayClick = { viewModel.jumpToToday() },
                accentColor = currentAppTheme.accentButton,
                onSettingsClick = { viewModel.openSettings() },
                onUpdatesClick = { viewModel.openUpdates() },
                onNewTaskClick = { viewModel.openCreateTask() }
            )

            // Dynamic Continuous Parabolic Rolling Ribbon Date Strip
            DateSelectorStrip(
                selectedDate = dayTasksState.date,
                onDateSelected = { date -> viewModel.selectDate(date) },
                selectedPillColor = currentAppTheme.accentButton,
                selectedTextColor = currentAppTheme.onAccentButton,
                domeArcColor = currentAppTheme.domeArcColor,
                domeFillColor = currentAppTheme.backgroundBottom
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Main Timeline & Tasks Container with tactile interactive drag feedback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dragOffsetX = 0f
                                velocityTracker.resetTracking()
                            },
                            onDragEnd = {
                                val velocityX = velocityTracker.calculateVelocity().x
                                if (dragOffsetX < -swipeThreshold || velocityX < -velocityThreshold) {
                                    // User swiped LEFT -> Switch to NEXT day
                                    currentViewModel.selectNextDay()
                                } else if (dragOffsetX > swipeThreshold || velocityX > velocityThreshold) {
                                    // User swiped RIGHT -> Switch to PREVIOUS day
                                    currentViewModel.selectPreviousDay()
                                }
                                dragOffsetX = 0f
                            },
                            onDragCancel = {
                                dragOffsetX = 0f
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetX += dragAmount
                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                            }
                        )
                    }
            ) {
                // Main Timeline & Tasks List with smooth loop/carousel animated transition between dates
                AnimatedContent(
                    targetState = dayTasksState.date,
                    transitionSpec = {
                        val targetDate = targetState
                        val initialDate = initialState

                        if (targetDate.isAfter(initialDate)) {
                            // Forward loop transition (carousel revolving to next day)
                            (slideInHorizontally(
                                animationSpec = tween(380, easing = CubicBezierEasing(0.18f, 1f, 0.32f, 1f))
                            ) { it } +
                                scaleIn(
                                    initialScale = 0.90f,
                                    animationSpec = tween(380, easing = CubicBezierEasing(0.18f, 1f, 0.32f, 1f))
                                ) +
                                fadeIn(animationSpec = tween(300))) togetherWith
                                (slideOutHorizontally(
                                    animationSpec = tween(340, easing = CubicBezierEasing(0.38f, 0f, 0.22f, 1f))
                                ) { -it } +
                                    scaleOut(
                                        targetScale = 0.90f,
                                        animationSpec = tween(340, easing = CubicBezierEasing(0.38f, 0f, 0.22f, 1f))
                                    ) +
                                    fadeOut(animationSpec = tween(220)))
                        } else {
                            // Backward loop transition (carousel revolving to previous day)
                            (slideInHorizontally(
                                animationSpec = tween(380, easing = CubicBezierEasing(0.18f, 1f, 0.32f, 1f))
                            ) { -it } +
                                scaleIn(
                                    initialScale = 0.90f,
                                    animationSpec = tween(380, easing = CubicBezierEasing(0.18f, 1f, 0.32f, 1f))
                                ) +
                                fadeIn(animationSpec = tween(300))) togetherWith
                                (slideOutHorizontally(
                                    animationSpec = tween(340, easing = CubicBezierEasing(0.38f, 0f, 0.22f, 1f))
                                ) { it } +
                                    scaleOut(
                                        targetScale = 0.90f,
                                        animationSpec = tween(340, easing = CubicBezierEasing(0.38f, 0f, 0.22f, 1f))
                                    ) +
                                    fadeOut(animationSpec = tween(220)))
                        }
                    },
                    label = "loopTasksTransition",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Real-time tactile drag feedback with subtle elastic tilt
                            translationX = animatedDragOffset * 0.40f
                            rotationZ = (animatedDragOffset / 140f).coerceIn(-1.5f, 1.5f)
                        }
                ) { currentDate ->
                    val currentTasks = dayTasksState.tasks

                    if (currentTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "No tasks for ${currentDate.dayOfMonth} ${currentDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap the + button below to add your first task for this day.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF7E92AC),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)
                        ) {
                            itemsIndexed(
                                items = currentTasks,
                                key = { _, task -> task.id }
                            ) { index, task ->
                                TaskRowItem(
                                    task = task,
                                    cardHeightDp = cardHeightDp,
                                    onCardClick = { viewModel.openEditTask(task) },
                                    onEditClick = { viewModel.openEditTask(task) },
                                    onDeleteClick = { viewModel.deleteTask(task.id) },
                                    onToggleComplete = { viewModel.toggleTaskComplete(task) },
                                    onStatusChange = { newStatus -> viewModel.updateTaskStatus(task, newStatus) }
                                )

                                // Dashed horizontal divider between task rows
                                if (index < currentTasks.size - 1) {
                                    TimelineHorizontalDivider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp, end = 4.dp, top = 16.dp, bottom = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add / Edit Task Modal Bottom Sheet
        if (isAddEditOpen) {
            AddEditTaskSheet(
                taskToEdit = taskToEdit,
                currentSelectedDate = dayTasksState.date,
                customColors = customColors,
                categories = categories,
                onAddCategory = { viewModel.addCategory(it) },
                onRemoveCategory = { viewModel.removeCategory(it) },
                onDismiss = { viewModel.closeAddEdit() },
                onSaveTask = { task -> viewModel.saveTask(task) }
            )
        }

        // Month & Year Picker Sheet (with year controls, 12 months, and Jump to Today)
        if (showMonthYearPicker) {
            MonthYearPickerSheet(
                selectedYearMonth = selectedYearMonth,
                onDismiss = { viewModel.toggleMonthYearPicker() },
                onSelectYearMonth = { ym -> viewModel.changeYearMonth(ym) }
            )
        }

        // Settings Bottom Sheet (Folder Size, Theme Pairs & Custom Colors)
        if (showSettings) {
            SettingsSheet(
                cardSizePreset = cardSizePreset,
                cardHeightDp = cardHeightDp,
                currentThemeId = appThemeId,
                customColors = customColors,
                onSaveSettings = { preset, height, themeId -> viewModel.saveSettings(preset, height, themeId) },
                onAddCustomColor = { viewModel.addCustomColor(it) },
                onRemoveCustomColor = { viewModel.removeCustomColor(it) },
                onDismiss = { viewModel.closeSettings() }
            )
        }

        // Updates Bottom Sheet
        if (showUpdates) {
            UpdatesSheet(
                onDismiss = { viewModel.closeUpdates() }
            )
        }
    }
}

@Composable
private fun TaskRowItem(
    task: Task,
    cardHeightDp: Int,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onStatusChange: (com.mightypen.tasc.model.TaskStatus) -> Unit
) {
    val palette = getCardPalette(task.colorTheme, task.customColorHex)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Timeline timestamps and live elapsed time counting connector line
        TimelineColumn(
            task = task,
            activeColor = palette.background,
            height = cardHeightDp.dp
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Right Column: Distinctive Custom Notched Task Card with Checkbox & Creative Done animations
        TaskCard(
            task = task,
            cardHeight = cardHeightDp.dp,
            modifier = Modifier.weight(1f),
            onCardClick = onCardClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onToggleComplete = onToggleComplete,
            onStatusChange = onStatusChange
        )
    }
}
