package com.mightypen.tasc.data

import android.content.Context
import com.mightypen.tasc.model.Task
import com.mightypen.tasc.model.TaskStatus
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
        return dbHelper.getTasksForDate(date)
    }

    fun getTaskById(id: Long): Task? {
        return dbHelper.getTaskById(id)
    }

    suspend fun loadTasksForDate(date: String) {
        currentDate = date
        val list = withContext(Dispatchers.IO) {
            dbHelper.getTasksForDate(date)
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
        val newStatus = if (task.status == TaskStatus.COMPLETED) TaskStatus.RUNNING else TaskStatus.COMPLETED
        val newProgress = if (newStatus == TaskStatus.COMPLETED) 100 else 50
        val updated = task.copy(status = newStatus, progress = newProgress)
        return updateTask(updated)
    }

    suspend fun updateTaskStatus(task: Task, status: TaskStatus): Boolean {
        val newProgress = when (status) {
            TaskStatus.COMPLETED -> 100
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
