package com.mightypen.tasc.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mightypen.tasc.NotificationHelper
import com.mightypen.tasc.data.TaskDatabaseHelper
import com.mightypen.tasc.model.TaskStatus

class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("TaskAlarmReceiver", "onReceive triggered with action: $action")

        try {
            when (action) {
                NotificationHelper.ACTION_TASK_REMINDER -> {
                    val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
                    val reminderMinutes = intent.getIntExtra(NotificationHelper.EXTRA_REMINDER_MINUTES, 0)
                    Log.d("TaskAlarmReceiver", "Processing reminder for taskId: $taskId, reminderMinutes: $reminderMinutes")
                    if (taskId != -1L) {
                        val dbHelper = TaskDatabaseHelper(context)
                        val task = dbHelper.getTaskById(taskId)
                        if (task != null) {
                            if (task.status != TaskStatus.COMPLETED) {
                                NotificationHelper.showTaskNotification(context, task, reminderMinutes)
                            } else {
                                Log.d("TaskAlarmReceiver", "Task $taskId is already COMPLETED, skipping notification")
                            }
                        } else {
                            Log.e("TaskAlarmReceiver", "Task $taskId not found in database")
                        }
                    }
                }

                NotificationHelper.ACTION_MARK_DONE -> {
                    val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
                    Log.d("TaskAlarmReceiver", "Marking task $taskId as DONE from notification action")
                    if (taskId != -1L) {
                        val dbHelper = TaskDatabaseHelper(context)
                        val task = dbHelper.getTaskById(taskId)
                        if (task != null) {
                            val elapsedProgress = com.mightypen.tasc.model.calculateElapsedProgress(task)
                            dbHelper.updateTask(task.copy(status = TaskStatus.COMPLETED, progress = elapsedProgress))
                            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancel(taskId.toInt())
                        }
                    }
                }

                Intent.ACTION_BOOT_COMPLETED -> {
                    Log.d("TaskAlarmReceiver", "BOOT_COMPLETED received, rescheduling all alarms")
                    NotificationHelper.rescheduleAllAlarms(context)
                }
            }
        } catch (e: Exception) {
            Log.e("TaskAlarmReceiver", "Error processing broadcast action: $action", e)
        }
    }
}
