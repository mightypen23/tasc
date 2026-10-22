package com.mightypen.tasc.data

import android.content.Context
import com.mightypen.tasc.model.Task
import com.mightypen.tasc.model.TaskStatus
import com.mightypen.tasc.model.calculateElapsedProgress
import com.mightypen.tasc.model.isOverdue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate

class TaskRepository(context: Context) {
    private val dbHelper = TaskDatabaseHelper(context.applicationContext)
    private var currentDate: String = LocalDate.now().toString()
    private val _tasksFlow = MutableStateFlow<List<Task>>(dbHelper.getTasksForDate(currentDate))
    val tasksFlow: StateFlow<List<Task>> = _tasksFlow.asStateFlow()

    fun getTasksForDateSync(date: String): List<Task> {
        val list = dbHelper.getTasksForDate(date)
        return checkAndPromoteOverdueTasks(list)
    }

    fun getTaskById(id: Long): Task? {
        return dbHelper.getTaskById(id)
    }

    private fun checkAndPromoteOverdueTasks(tasks: List<Task>): List<Task> {
        val now = java.time.LocalDateTime.now()
        return tasks.map { task ->
            if (task.status != TaskStatus.COMPLETED &&
                task.status != TaskStatus.REJECTED &&
                task.status != TaskStatus.OVERDUE &&
                task.isOverdue(now)
            ) {
                val overdueTask = task.copy(status = TaskStatus.OVERDUE, progress = 100)
                dbHelper.updateTask(overdueTask)
                overdueTask
            } else {
                task
            }
        }
    }

    suspend fun loadTasksForDate(date: String) {
        currentDate = date
        val list = withContext(Dispatchers.IO) {
            val raw = dbHelper.getTasksForDate(date)
            checkAndPromoteOverdueTasks(raw)
        }
        _tasksFlow.value = list
    }

    suspend fun addTask(task: Task): Long {
        return withContext(Dispatchers.IO) {
            val id = dbHelper.insertTask(task)
            loadTasksForDate(currentDate)
            id
        }
    }

    suspend fun updateTask(task: Task): Boolean {
        return withContext(Dispatchers.IO) {
            val success = dbHelper.updateTask(task)
            loadTasksForDate(currentDate)
            success
        }
    }

    suspend fun deleteTask(id: Long): Boolean {
        return withContext(Dispatchers.IO) {
            val success = dbHelper.deleteTask(id)
            loadTasksForDate(currentDate)
            success
        }
    }

    suspend fun toggleTaskCompleted(task: Task): Boolean {
        val isNowCompleted = task.status != TaskStatus.COMPLETED
        val updated = if (isNowCompleted) {
            // Task marked complete before or at interval end: freeze at current elapsed percentage
            val progressAtCompletion = task.calculateElapsedProgress()
            task.copy(status = TaskStatus.COMPLETED, progress = progressAtCompletion)
        } else {
            // Uncompleting: if interval has already passed, revert to OVERDUE; else RUNNING
            val isNowOverdue = task.isOverdue()
            val restoredStatus = if (isNowOverdue) TaskStatus.OVERDUE else TaskStatus.RUNNING
            val restoredProgress = if (isNowOverdue) 100 else 0
            task.copy(status = restoredStatus, progress = restoredProgress)
        }
        return updateTask(updated)
    }

    suspend fun updateTaskStatus(task: Task, status: TaskStatus): Boolean {
        val newProgress = when (status) {
            TaskStatus.COMPLETED -> task.calculateElapsedProgress()
            TaskStatus.OVERDUE -> 100
            TaskStatus.REJECTED -> 0
            TaskStatus.UPCOMING -> 0
            TaskStatus.RUNNING -> if (task.progress == 0 || task.progress == 100) 50 else task.progress
        }
        val updated = task.copy(status = status, progress = newProgress)
        return updateTask(updated)
    }

    suspend fun getAllRecordedDates(): List<String> {
        return withContext(Dispatchers.IO) {
            dbHelper.getAllTasks().map { it.date }.distinct()
        }
    }
}
