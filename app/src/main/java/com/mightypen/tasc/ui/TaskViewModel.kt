package com.mightypen.tasc.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mightypen.tasc.data.TaskRepository
import com.mightypen.tasc.model.Task
import com.mightypen.tasc.model.TaskStatus
import com.mightypen.tasc.model.isOverdue
import com.mightypen.tasc.ui.components.CalendarDayItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

data class DayTasksState(
    val date: LocalDate,
    val tasks: List<Task>,
    val direction: Int = 0 // +1 for next day/future, -1 for prev day/past, 0 for in-place update
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application)
    private val initialDate = LocalDate.now()

    // Current selected date (defaults to today)
    private val _selectedDate = MutableStateFlow(initialDate)
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Combined atomic state of date and its tasks for silky-smooth, glitch-free transitions
    private val _dayTasksState = MutableStateFlow(
        DayTasksState(
            date = initialDate,
            tasks = repository.getTasksForDateSync(initialDate.toString()),
            direction = 0
        )
    )
    val dayTasksState: StateFlow<DayTasksState> = _dayTasksState.asStateFlow()

    // Currently selected YearMonth
    private val _selectedYearMonth = MutableStateFlow(YearMonth.from(initialDate))
    val selectedYearMonth: StateFlow<YearMonth> = _selectedYearMonth.asStateFlow()

    // Human readable label (e.g. "October 2026")
    val monthLabel: StateFlow<String> = _selectedYearMonth.combine(_selectedDate) { ym, _ ->
        val monthName = ym.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        "$monthName ${ym.year}"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private fun buildCalendarDays(ym: YearMonth): List<CalendarDayItem> {
        val daysInMonth = ym.lengthOfMonth()
        return (1..daysInMonth).map { dayNum ->
            val date = ym.atDay(dayNum)
            CalendarDayItem(date = date)
        }
    }

    // Complete list of days for the selected month (1 to lengthOfMonth)
    val calendarDays: StateFlow<List<CalendarDayItem>> = _selectedYearMonth.map { ym ->
        buildCalendarDays(ym)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        buildCalendarDays(_selectedYearMonth.value)
    )

    private val preferences = com.mightypen.tasc.data.TaskPreferences(application)

    val cardSizePreset: StateFlow<com.mightypen.tasc.data.TaskCardSizePreset> = preferences.cardSizePreset
    val cardHeightDp: StateFlow<Int> = preferences.cardHeightDp
    val customColors: StateFlow<List<String>> = preferences.customColors
    val categories: StateFlow<List<String>> = preferences.categories
    val appThemeId: StateFlow<String> = preferences.appThemeId

    val currentAppTheme: StateFlow<com.mightypen.tasc.ui.theme.AppThemePair> = preferences.appThemeId.map { themeId ->
        com.mightypen.tasc.ui.theme.AppThemes.fromId(themeId)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        com.mightypen.tasc.ui.theme.AppThemes.fromId(preferences.appThemeId.value)
    )

    val tasks: StateFlow<List<Task>> = repository.tasksFlow

    private val _isAddEditOpen = MutableStateFlow(false)
    val isAddEditOpen: StateFlow<Boolean> = _isAddEditOpen.asStateFlow()

    private val _taskToEdit = MutableStateFlow<Task?>(null)
    val taskToEdit: StateFlow<Task?> = _taskToEdit.asStateFlow()

    private val _showMonthYearPicker = MutableStateFlow(false)
    val showMonthYearPicker: StateFlow<Boolean> = _showMonthYearPicker.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _showUpdates = MutableStateFlow(false)
    val showUpdates: StateFlow<Boolean> = _showUpdates.asStateFlow()

    fun openSettings() { _showSettings.value = true }
    fun closeSettings() { _showSettings.value = false }

    fun openUpdates() { _showUpdates.value = true }
    fun closeUpdates() { _showUpdates.value = false }

    fun saveSettings(preset: com.mightypen.tasc.data.TaskCardSizePreset, heightDp: Int, themeId: String) {
        preferences.saveAllSettings(preset, heightDp, themeId)
    }

    fun setCardSizePreset(preset: com.mightypen.tasc.data.TaskCardSizePreset) {
        preferences.setCardSizePreset(preset)
    }

    fun setCustomCardHeight(heightDp: Int) {
        preferences.setCustomCardHeight(heightDp)
    }

    fun setAppThemeId(themeId: String) {
        preferences.setAppThemeId(themeId)
    }

    fun addCustomColor(hexCode: String): Boolean {
        return preferences.addCustomColor(hexCode)
    }

    fun removeCustomColor(hexCode: String) {
        preferences.removeCustomColor(hexCode)
    }

    fun addCategory(name: String): Boolean {
        return preferences.addCategory(name)
    }

    fun removeCategory(name: String) {
        preferences.removeCategory(name)
    }

    init {
        // Load tasks for today on startup
        refreshCurrentDayTasks(direction = 0)

        // Real-time ticker to auto-mark tasks as OVERDUE when their interval finishes
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(4000L)
                val currentTasks = _dayTasksState.value.tasks
                val now = java.time.LocalDateTime.now()
                val anyBecameOverdue = currentTasks.any {
                    it.status != TaskStatus.COMPLETED &&
                    it.status != TaskStatus.REJECTED &&
                    it.status != TaskStatus.OVERDUE &&
                    it.isOverdue(now)
                }
                if (anyBecameOverdue) {
                    refreshCurrentDayTasks(direction = 0)
                }
            }
        }
    }

    private fun refreshCurrentDayTasks(direction: Int = 0) {
        val date = _selectedDate.value
        val list = repository.getTasksForDateSync(date.toString())
        _dayTasksState.value = DayTasksState(
            date = date,
            tasks = list,
            direction = direction
        )
        viewModelScope.launch {
            repository.loadTasksForDate(date.toString())
        }
    }

    fun selectDate(date: LocalDate) {
        val oldDate = _selectedDate.value
        if (oldDate == date) return
        val direction = if (date.isAfter(oldDate)) 1 else -1

        _selectedDate.value = date
        val ym = YearMonth.from(date)
        if (ym != _selectedYearMonth.value) {
            _selectedYearMonth.value = ym
        }

        // Fetch synchronously for atomic single-frame transition
        val currentTasks = repository.getTasksForDateSync(date.toString())
        _dayTasksState.value = DayTasksState(
            date = date,
            tasks = currentTasks,
            direction = direction
        )

        viewModelScope.launch {
            repository.loadTasksForDate(date.toString())
        }
    }

    fun selectNextDay() {
        selectDate(_selectedDate.value.plusDays(1))
    }

    fun selectPreviousDay() {
        selectDate(_selectedDate.value.minusDays(1))
    }

    fun changeYearMonth(newYearMonth: YearMonth) {
        _selectedYearMonth.value = newYearMonth
        val today = LocalDate.now()

        val newDate = if (YearMonth.from(today) == newYearMonth) {
            today
        } else {
            val currentDay = _selectedDate.value.dayOfMonth.coerceIn(1, newYearMonth.lengthOfMonth())
            newYearMonth.atDay(currentDay)
        }
        selectDate(newDate)
    }

    fun jumpToToday() {
        val today = LocalDate.now()
        _selectedYearMonth.value = YearMonth.from(today)
        selectDate(today)
    }

    fun toggleMonthYearPicker() {
        _showMonthYearPicker.value = !_showMonthYearPicker.value
    }

    fun openCreateTask() {
        _taskToEdit.value = null
        _isAddEditOpen.value = true
    }

    fun openEditTask(task: Task) {
        _taskToEdit.value = task
        _isAddEditOpen.value = true
    }

    fun closeAddEdit() {
        _isAddEditOpen.value = false
        _taskToEdit.value = null
    }

    fun saveTask(task: Task) {
        viewModelScope.launch {
            val savedTask = if (task.id == 0L) {
                val newId = repository.addTask(task)
                task.copy(id = newId)
            } else {
                repository.updateTask(task)
                task
            }
            if (savedTask.status != TaskStatus.COMPLETED) {
                com.mightypen.tasc.NotificationHelper.scheduleTaskReminder(getApplication(), savedTask)
            } else {
                com.mightypen.tasc.NotificationHelper.cancelTaskReminder(getApplication(), savedTask.id)
            }
            refreshCurrentDayTasks(direction = 0)
            closeAddEdit()
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            com.mightypen.tasc.NotificationHelper.cancelTaskReminder(getApplication(), id)
            repository.deleteTask(id)
            refreshCurrentDayTasks(direction = 0)
        }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            val isNowCompleted = task.status != TaskStatus.COMPLETED
            if (isNowCompleted) {
                com.mightypen.tasc.NotificationHelper.cancelTaskReminder(getApplication(), task.id)
            } else {
                com.mightypen.tasc.NotificationHelper.scheduleTaskReminder(getApplication(), task.copy(status = TaskStatus.RUNNING))
            }
            repository.toggleTaskCompleted(task)
            refreshCurrentDayTasks(direction = 0)
        }
    }

    fun updateTaskStatus(task: Task, status: TaskStatus) {
        viewModelScope.launch {
            if (status == TaskStatus.COMPLETED) {
                com.mightypen.tasc.NotificationHelper.cancelTaskReminder(getApplication(), task.id)
            } else {
                com.mightypen.tasc.NotificationHelper.scheduleTaskReminder(getApplication(), task.copy(status = status))
            }
            repository.updateTaskStatus(task, status)
            refreshCurrentDayTasks(direction = 0)
        }
    }
}
