package com.mightypen.tasc.model

import java.time.LocalDate

enum class TaskStatus(val displayName: String) {
    COMPLETED("Completed"),
    REJECTED("Rejected"),
    RUNNING("Running"),
    UPCOMING("Upcoming");

    companion object {
        fun fromString(value: String): TaskStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: RUNNING
        }
    }
}

enum class TaskColorTheme {
    LIME,
    CYAN,
    LAVENDER,
    YELLOW,
    CORAL;

    companion object {
        fun fromString(value: String): TaskColorTheme {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: LIME
        }
    }
}

data class Task(
    val id: Long = 0,
    val title: String,
    val subtitle: String,
    val startTime: String,
    val endTime: String,
    val status: TaskStatus = TaskStatus.RUNNING,
    val progress: Int = 0,
    val colorTheme: TaskColorTheme = TaskColorTheme.LIME,
    val customColorHex: String? = null,
    val date: String = LocalDate.now().toString(), // ISO "YYYY-MM-DD"
    val reminderMinutesBefore: Int? = 0, // null = No reminder, 0 = On time, 5, 10, 15, etc.
    val createdAt: Long = System.currentTimeMillis()
)
