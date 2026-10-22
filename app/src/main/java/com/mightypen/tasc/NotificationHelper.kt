package com.mightypen.tasc

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.mightypen.tasc.data.TaskDatabaseHelper
import com.mightypen.tasc.model.Task
import com.mightypen.tasc.model.TaskColorTheme
import com.mightypen.tasc.receiver.TaskAlarmReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

object NotificationHelper {
    private const val TAG = "NotificationHelper"
    const val CHANNEL_ID = "tasc_reminder_channel"
    const val CHANNEL_NAME = "Task Reminders"

    const val ACTION_TASK_REMINDER = "com.mightypen.tasc.ACTION_TASK_REMINDER"
    const val ACTION_MARK_DONE = "com.mightypen.tasc.ACTION_MARK_DONE"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_REMINDER_MINUTES = "extra_reminder_minutes"

    /**
     * Initializes the High-Priority notification channel required for Android 8.0+.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts and reminders for scheduled tasks"
                    enableLights(true)
                    lightColor = AndroidColor.parseColor("#D4F698")
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 200, 300)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel $CHANNEL_ID created with IMPORTANCE_HIGH")
            }
        }
    }

    /**
     * Parse task date and time string into a LocalDateTime.
     * Supports "10:00 am", "10 am", "02:30 pm", "2 pm", etc.
     */
    fun parseTaskDateTime(dateStr: String, timeStr: String): LocalDateTime? {
        return try {
            val date = LocalDate.parse(dateStr.trim())
            val lower = timeStr.trim().lowercase(Locale.US)
            val isPm = lower.contains("pm")
            val digitsOnly = lower.replace("am", "").replace("pm", "").trim()

            val hour: Int
            val minute: Int
            if (digitsOnly.contains(":")) {
                val parts = digitsOnly.split(":")
                var h = parts[0].trim().toIntOrNull() ?: return null
                minute = parts[1].trim().toIntOrNull() ?: 0
                if (isPm && h < 12) h += 12
                if (!isPm && h == 12) h = 0
                hour = h
            } else {
                var h = digitsOnly.toIntOrNull() ?: return null
                minute = 0
                if (isPm && h < 12) h += 12
                if (!isPm && h == 12) h = 0
                hour = h
            }

            date.atTime(LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59)))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse date '$dateStr' or time '$timeStr'", e)
            null
        }
    }

    /**
     * Schedules an exact AlarmManager alert for the specified task.
     * If reminderMinutesBefore is null, any existing alarm is cancelled.
     */
    fun scheduleTaskReminder(context: Context, task: Task) {
        val reminderMinutes = task.reminderMinutesBefore
        if (reminderMinutes == null) {
            cancelTaskReminder(context, task.id)
            return
        }

        val taskDateTime = parseTaskDateTime(task.date, task.startTime) ?: return
        val reminderDateTime = taskDateTime.minusMinutes(reminderMinutes.toLong())
        val triggerEpochMillis = reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()

        Log.d(TAG, "Scheduling task '${task.title}' for $reminderDateTime (epoch: $triggerEpochMillis, now: $now)")

        // If task starts right now (within current minute or up to 45s ago), trigger immediately!
        if (triggerEpochMillis <= now) {
            if (now - triggerEpochMillis < 60_000L) {
                Log.d(TAG, "Task '${task.title}' starts right now, triggering immediate notification")
                showTaskNotification(context, task, reminderMinutes)
            } else {
                Log.d(TAG, "Task '${task.title}' reminder time has passed, skipping alarm schedule")
            }
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_TASK_REMINDER
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_REMINDER_MINUTES, reminderMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            }
            Log.d(TAG, "Alarm successfully set for task '${task.title}' at epoch $triggerEpochMillis")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for task '${task.title}'", e)
        }
    }

    /**
     * Cancels any scheduled reminder alarm for a task.
     */
    fun cancelTaskReminder(context: Context, taskId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
                action = ACTION_TASK_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Color palette for notification styling.
     */
    data class NotificationPalette(
        val backgroundColor: Int,
        val textColor: Int,
        val subtextColor: Int
    )

    private fun getPalette(task: Task): NotificationPalette {
        if (!task.customColorHex.isNullOrBlank()) {
            try {
                val clean = if (task.customColorHex.startsWith("#")) task.customColorHex else "#${task.customColorHex}"
                val parsed = AndroidColor.parseColor(clean)
                val luminance = ColorUtils.calculateLuminance(parsed)
                val isLight = luminance > 0.42
                return NotificationPalette(
                    backgroundColor = parsed,
                    textColor = if (isLight) AndroidColor.parseColor("#111D11") else AndroidColor.WHITE,
                    subtextColor = if (isLight) AndroidColor.parseColor("#38483B") else AndroidColor.parseColor("#D0D8E4")
                )
            } catch (_: Exception) {}
        }

        return when (task.colorTheme) {
            TaskColorTheme.LIME -> NotificationPalette(
                backgroundColor = AndroidColor.parseColor("#D2F69A"),
                textColor = AndroidColor.parseColor("#132214"),
                subtextColor = AndroidColor.parseColor("#38523A")
            )
            TaskColorTheme.CYAN -> NotificationPalette(
                backgroundColor = AndroidColor.parseColor("#37C6EE"),
                textColor = AndroidColor.parseColor("#0B2A3D"),
                subtextColor = AndroidColor.parseColor("#1A5676")
            )
            TaskColorTheme.LAVENDER -> NotificationPalette(
                backgroundColor = AndroidColor.parseColor("#D3C3E6"),
                textColor = AndroidColor.parseColor("#2A1A38"),
                subtextColor = AndroidColor.parseColor("#513E65")
            )
            TaskColorTheme.YELLOW -> NotificationPalette(
                backgroundColor = AndroidColor.parseColor("#E5FA8E"),
                textColor = AndroidColor.parseColor("#232E14"),
                subtextColor = AndroidColor.parseColor("#4C5E2D")
            )
            TaskColorTheme.CORAL -> NotificationPalette(
                backgroundColor = AndroidColor.parseColor("#FFB3BA"),
                textColor = AndroidColor.parseColor("#381418"),
                subtextColor = AndroidColor.parseColor("#6B2D34")
            )
        }
    }

    /**
     * Renders a vector/shape drawable into a colored Bitmap to ensure 100% compatibility
     * with RemoteViews across all Android OEM versions without reflection errors.
     */
    private fun createColoredBitmap(context: Context, drawableResId: Int, color: Int, widthPx: Int, heightPx: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val drawable = ContextCompat.getDrawable(context, drawableResId)?.mutate()
        drawable?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        drawable?.setBounds(0, 0, widthPx, heightPx)
        drawable?.draw(canvas)
        return bitmap
    }

    /**
     * Builds and shows a custom RemoteViews notification styled as a task folder card.
     */
    fun showTaskNotification(context: Context, task: Task, reminderMinutes: Int? = 0) {
        createNotificationChannel(context)

        val palette = getPalette(task)
        val categoryText = task.subtitle.ifBlank { "General" }.uppercase(Locale.US)

        val timingText = when (reminderMinutes) {
            null, 0 -> "Starting on time • ${task.startTime}"
            else -> "Starts in $reminderMinutes mins • ${task.startTime}"
        }

        // Tap content intent -> opens MainActivity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action "Mark Done" -> sends broadcast to TaskAlarmReceiver
        val markDoneIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_TASK_ID, task.id)
        }
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context,
            (task.id + 100000).toInt(),
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val density = context.resources.displayMetrics.density
            val tabWidth = (72 * density).toInt()
            val tabHeight = (18 * density).toInt()
            val tabBitmap = createColoredBitmap(context, R.drawable.notification_folder_tab_bg, palette.backgroundColor, tabWidth, tabHeight)

            val bodyWidth = (360 * density).toInt()
            val bodyHeight = (80 * density).toInt()
            val bodyBitmap = createColoredBitmap(context, R.drawable.notification_folder_body_bg, palette.backgroundColor, bodyWidth, bodyHeight)

            // 1. Collapsed View
            val collapsedViews = RemoteViews(context.packageName, R.layout.notification_folder_collapsed).apply {
                setImageViewBitmap(R.id.notification_tab_bg, tabBitmap)
                setImageViewBitmap(R.id.notification_body_bg, bodyBitmap)

                setTextViewText(R.id.notification_category, categoryText)
                setTextColor(R.id.notification_category, palette.textColor)

                setTextViewText(R.id.notification_time_tag, timingText)

                setTextViewText(R.id.notification_title, task.title)
                setTextColor(R.id.notification_title, palette.textColor)

                val subtitleText = "${task.subtitle.ifBlank { "Task" }} • ${task.startTime}"
                setTextViewText(R.id.notification_subtitle, subtitleText)
                setTextColor(R.id.notification_subtitle, palette.subtextColor)
            }

            // 2. Expanded View
            val expandedViews = RemoteViews(context.packageName, R.layout.notification_folder_expanded).apply {
                setImageViewBitmap(R.id.notification_tab_bg, tabBitmap)
                setImageViewBitmap(R.id.notification_body_bg, bodyBitmap)

                setTextViewText(R.id.notification_category, categoryText)
                setTextColor(R.id.notification_category, palette.textColor)

                setTextViewText(R.id.notification_time_tag, timingText)

                setTextViewText(R.id.notification_title, task.title)
                setTextColor(R.id.notification_title, palette.textColor)

                setTextViewText(R.id.notification_subtitle, task.subtitle.ifBlank { "Task schedule" })
                setTextColor(R.id.notification_subtitle, palette.subtextColor)

                val timeRangeText = "🕒 ${task.startTime} - ${task.endTime}"
                setTextViewText(R.id.notification_time, timeRangeText)
                setTextColor(R.id.notification_time, palette.textColor)

                setTextColor(R.id.notification_btn_done, palette.textColor)
                setOnClickPendingIntent(R.id.notification_btn_done, markDonePendingIntent)

                setTextColor(R.id.notification_btn_open, palette.textColor)
                setOnClickPendingIntent(R.id.notification_btn_open, openAppPendingIntent)
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_folder)
                .setCustomContentView(collapsedViews)
                .setCustomBigContentView(expandedViews)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setContentTitle(task.title)
                .setContentText("${task.subtitle} (${task.startTime})")
                .setContentIntent(openAppPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setColor(palette.backgroundColor)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(task.id.toInt(), notification)
            Log.d(TAG, "Successfully posted custom folder notification for task id ${task.id} ('${task.title}')")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating custom folder notification, falling back to standard notification", e)
            try {
                val fallbackNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_folder)
                    .setContentTitle(task.title)
                    .setContentText("${task.subtitle.ifBlank { "Task" }} • ${task.startTime}")
                    .setStyle(NotificationCompat.BigTextStyle().bigText("🕒 ${task.startTime} - ${task.endTime}\n${task.subtitle.ifBlank { "Scheduled task" }}"))
                    .setContentIntent(openAppPendingIntent)
                    .addAction(R.drawable.ic_notification_folder, "✓ Mark Done", markDonePendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setColor(palette.backgroundColor)
                    .build()

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(task.id.toInt(), fallbackNotification)
                Log.d(TAG, "Successfully posted fallback standard notification for task id ${task.id}")
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Fallback notification also failed", fallbackEx)
            }
        }
    }

    /**
     * Reschedules all alarms on device reboot or startup.
     */
    fun rescheduleAllAlarms(context: Context) {
        val dbHelper = TaskDatabaseHelper(context)
        val allTasks = dbHelper.getAllTasks()
        for (task in allTasks) {
            if (task.reminderMinutesBefore != null && task.status != com.mightypen.tasc.model.TaskStatus.COMPLETED) {
                scheduleTaskReminder(context, task)
            }
        }
    }
}
