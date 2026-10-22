package com.mightypen.tasc.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class TaskStatus(val displayName: String) {
    COMPLETED("Completed"),
    REJECTED("Rejected"),
    RUNNING("Running"),
    UPCOMING("Upcoming"),
    OVERDUE("Overdue");

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

/**
 * Calculates the percentage of time (0..100) elapsed between startTime and endTime.
 * If current time is before startTime, returns 0.
 * If current time is after endTime, returns 100.
 */
fun Task.calculateElapsedProgress(now: LocalDateTime = LocalDateTime.now()): Int {
    val safeDate = date.ifBlank { LocalDate.now().toString() }
    val start = com.mightypen.tasc.NotificationHelper.parseTaskDateTime(safeDate, startTime) ?: return 100
    val endRaw = com.mightypen.tasc.NotificationHelper.parseTaskDateTime(safeDate, endTime) ?: return 100
    val end = if (endRaw.isBefore(start)) endRaw.plusDays(1) else endRaw

    if (now.isBefore(start)) return 0
    if (!now.isBefore(end)) return 100

    val totalSecs = ChronoUnit.SECONDS.between(start, end).coerceAtLeast(1)
    val elapsedSecs = ChronoUnit.SECONDS.between(start, now).coerceIn(0, totalSecs)
    return ((elapsedSecs.toDouble() / totalSecs.toDouble()) * 100).toInt().coerceIn(1, 99)
}

/**
 * Checks whether the task's scheduled interval is in the past.
 * Completed and Rejected tasks are not considered overdue.
 */
fun Task.isOverdue(now: LocalDateTime = LocalDateTime.now()): Boolean {
    if (status == TaskStatus.COMPLETED || status == TaskStatus.REJECTED) return false
    val safeDate = date.ifBlank { LocalDate.now().toString() }
    val start = com.mightypen.tasc.NotificationHelper.parseTaskDateTime(safeDate, startTime) ?: return false
    val endRaw = com.mightypen.tasc.NotificationHelper.parseTaskDateTime(safeDate, endTime) ?: return false
    val end = if (endRaw.isBefore(start)) endRaw.plusDays(1) else endRaw
    return !now.isBefore(end)
}
