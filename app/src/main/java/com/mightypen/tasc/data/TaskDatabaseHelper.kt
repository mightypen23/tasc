package com.mightypen.tasc.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mightypen.tasc.model.Task
import com.mightypen.tasc.model.TaskColorTheme
import com.mightypen.tasc.model.TaskStatus
import java.time.LocalDate

class TaskDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tasc_local.db"
        private const val DATABASE_VERSION = 6

        const val TABLE_TASKS = "tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_SUBTITLE = "subtitle"
        const val COLUMN_START_TIME = "start_time"
        const val COLUMN_END_TIME = "end_time"
        const val COLUMN_STATUS = "status"
        const val COLUMN_PROGRESS = "progress"
        const val COLUMN_COLOR_THEME = "color_theme"
        const val COLUMN_CUSTOM_COLOR = "custom_color"
        const val COLUMN_DATE = "date"
        const val COLUMN_REMINDER_MINUTES = "reminder_minutes"
        const val COLUMN_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_SUBTITLE TEXT NOT NULL,
                $COLUMN_START_TIME TEXT NOT NULL,
                $COLUMN_END_TIME TEXT NOT NULL,
                $COLUMN_STATUS TEXT NOT NULL,
                $COLUMN_PROGRESS INTEGER NOT NULL,
                $COLUMN_COLOR_THEME TEXT NOT NULL,
                $COLUMN_CUSTOM_COLOR TEXT,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_REMINDER_MINUTES INTEGER,
                $COLUMN_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)

        seedInitialTasks(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN $COLUMN_CUSTOM_COLOR TEXT")
            } catch (e: Exception) {
                // Column might already exist
            }
        }
        if (oldVersion < 6) {
            try {
                db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN $COLUMN_REMINDER_MINUTES INTEGER")
            } catch (e: Exception) {
                // Column might already exist
            }
        }
    }

    private fun seedInitialTasks(db: SQLiteDatabase) {
        val today = LocalDate.now()
        val todayStr = today.toString()
        val yesterdayStr = today.minusDays(1).toString()
        val tomorrowStr = today.plusDays(1).toString()
        val dayAfterTomorrowStr = today.plusDays(2).toString()

        val sampleTasks = mutableListOf<Task>()

        // 1. Yesterday's tasks (completed tasks)
        sampleTasks.addAll(
            listOf(
                Task(
                    title = "Design System Review",
                    subtitle = "Client project",
                    startTime = "09 am",
                    endTime = "11 am",
                    status = TaskStatus.COMPLETED,
                    progress = 100,
                    colorTheme = TaskColorTheme.CYAN,
                    date = yesterdayStr
                ),
                Task(
                    title = "Sprint Retrospective",
                    subtitle = "Engineering sync",
                    startTime = "02 pm",
                    endTime = "03:30 pm",
                    status = TaskStatus.COMPLETED,
                    progress = 100,
                    colorTheme = TaskColorTheme.LAVENDER,
                    date = yesterdayStr
                )
            )
        )

        // 2. Today's tasks (current active day)
        sampleTasks.addAll(
            listOf(
                Task(
                    title = "Web Design",
                    subtitle = "Client project",
                    startTime = "10 am",
                    endTime = "02 pm",
                    status = TaskStatus.COMPLETED,
                    progress = 100,
                    colorTheme = TaskColorTheme.LIME,
                    date = todayStr
                ),
                Task(
                    title = "Family Program",
                    subtitle = "Family task",
                    startTime = "03 pm",
                    endTime = "04 pm",
                    status = TaskStatus.REJECTED,
                    progress = 0,
                    colorTheme = TaskColorTheme.CYAN,
                    date = todayStr
                ),
                Task(
                    title = "Market Research",
                    subtitle = "Company task",
                    startTime = "06 pm",
                    endTime = "08 pm",
                    status = TaskStatus.RUNNING,
                    progress = 72,
                    colorTheme = TaskColorTheme.LAVENDER,
                    date = todayStr
                ),
                Task(
                    title = "Upcoming Sprint Review",
                    subtitle = "Engineering sync",
                    startTime = "10 pm",
                    endTime = "11:30 pm",
                    status = TaskStatus.UPCOMING,
                    progress = 0,
                    colorTheme = TaskColorTheme.YELLOW,
                    date = todayStr
                )
            )
        )

        // 3. Tomorrow's tasks
        sampleTasks.addAll(
            listOf(
                Task(
                    title = "Mobile App Architecture",
                    subtitle = "Company task",
                    startTime = "09 am",
                    endTime = "11:30 am",
                    status = TaskStatus.UPCOMING,
                    progress = 0,
                    colorTheme = TaskColorTheme.LIME,
                    date = tomorrowStr
                ),
                Task(
                    title = "Product Roadmap Review",
                    subtitle = "Product team",
                    startTime = "01 pm",
                    endTime = "03 pm",
                    status = TaskStatus.RUNNING,
                    progress = 35,
                    colorTheme = TaskColorTheme.YELLOW,
                    date = tomorrowStr
                ),
                Task(
                    title = "Gym & Fitness",
                    subtitle = "Personal health",
                    startTime = "05 pm",
                    endTime = "06:30 pm",
                    status = TaskStatus.UPCOMING,
                    progress = 0,
                    colorTheme = TaskColorTheme.CYAN,
                    date = tomorrowStr
                )
            )
        )

        // 4. Day after tomorrow's tasks
        sampleTasks.addAll(
            listOf(
                Task(
                    title = "UI Component Library",
                    subtitle = "Design system",
                    startTime = "10 am",
                    endTime = "01 pm",
                    status = TaskStatus.UPCOMING,
                    progress = 0,
                    colorTheme = TaskColorTheme.LAVENDER,
                    date = dayAfterTomorrowStr
                ),
                Task(
                    title = "User Feedback Interviews",
                    subtitle = "User research",
                    startTime = "03 pm",
                    endTime = "05 pm",
                    status = TaskStatus.UPCOMING,
                    progress = 0,
                    colorTheme = TaskColorTheme.LIME,
                    date = dayAfterTomorrowStr
                )
            )
        )

        // 5. Also seed January 12, 2023 from the original design
        if (todayStr != "2023-01-12") {
            sampleTasks.addAll(
                listOf(
                    Task(
                        title = "Web Design",
                        subtitle = "Client project",
                        startTime = "10 am",
                        endTime = "02 pm",
                        status = TaskStatus.COMPLETED,
                        progress = 100,
                        colorTheme = TaskColorTheme.LIME,
                        date = "2023-01-12"
                    ),
                    Task(
                        title = "Family Program",
                        subtitle = "Family task",
                        startTime = "03 pm",
                        endTime = "04 pm",
                        status = TaskStatus.REJECTED,
                        progress = 0,
                        colorTheme = TaskColorTheme.CYAN,
                        date = "2023-01-12"
                    ),
                    Task(
                        title = "Market Research",
                        subtitle = "Company task",
                        startTime = "06 pm",
                        endTime = "08 pm",
                        status = TaskStatus.RUNNING,
                        progress = 72,
                        colorTheme = TaskColorTheme.LAVENDER,
                        date = "2023-01-12"
                    ),
                    Task(
                        title = "Design System Sync",
                        subtitle = "Client project",
                        startTime = "10 pm",
                        endTime = "11:30 pm",
                        status = TaskStatus.UPCOMING,
                        progress = 0,
                        colorTheme = TaskColorTheme.YELLOW,
                        date = "2023-01-12"
                    )
                )
            )
        }

        for (task in sampleTasks) {
            val values = ContentValues().apply {
                put(COLUMN_TITLE, task.title)
                put(COLUMN_SUBTITLE, task.subtitle)
                put(COLUMN_START_TIME, task.startTime)
                put(COLUMN_END_TIME, task.endTime)
                put(COLUMN_STATUS, task.status.name)
                put(COLUMN_PROGRESS, task.progress)
                put(COLUMN_COLOR_THEME, task.colorTheme.name)
                put(COLUMN_DATE, task.date)
                put(COLUMN_CREATED_AT, task.createdAt)
            }
            db.insert(TABLE_TASKS, null, values)
        }
    }

    fun insertTask(task: Task): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_SUBTITLE, task.subtitle)
            put(COLUMN_START_TIME, task.startTime)
            put(COLUMN_END_TIME, task.endTime)
            put(COLUMN_STATUS, task.status.name)
            put(COLUMN_PROGRESS, task.progress)
            put(COLUMN_COLOR_THEME, task.colorTheme.name)
            put(COLUMN_CUSTOM_COLOR, task.customColorHex)
            put(COLUMN_DATE, task.date)
            if (task.reminderMinutesBefore != null) {
                put(COLUMN_REMINDER_MINUTES, task.reminderMinutesBefore)
            } else {
                putNull(COLUMN_REMINDER_MINUTES)
            }
            put(COLUMN_CREATED_AT, task.createdAt)
        }
        return db.insert(TABLE_TASKS, null, values)
    }

    fun updateTask(task: Task): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_SUBTITLE, task.subtitle)
            put(COLUMN_START_TIME, task.startTime)
            put(COLUMN_END_TIME, task.endTime)
            put(COLUMN_STATUS, task.status.name)
            put(COLUMN_PROGRESS, task.progress)
            put(COLUMN_COLOR_THEME, task.colorTheme.name)
            put(COLUMN_CUSTOM_COLOR, task.customColorHex)
            put(COLUMN_DATE, task.date)
            if (task.reminderMinutesBefore != null) {
                put(COLUMN_REMINDER_MINUTES, task.reminderMinutesBefore)
            } else {
                putNull(COLUMN_REMINDER_MINUTES)
            }
        }
        return db.update(TABLE_TASKS, values, "$COLUMN_ID = ?", arrayOf(task.id.toString())) > 0
    }

    fun deleteTask(id: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_TASKS, "$COLUMN_ID = ?", arrayOf(id.toString())) > 0
    }

    private fun cursorToTask(cursor: android.database.Cursor): Task {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
        val subtitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBTITLE))
        val startTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME))
        val endTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME))
        val statusStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
        val progress = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS))
        val colorThemeStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR_THEME))
        val customColorIdx = cursor.getColumnIndex(COLUMN_CUSTOM_COLOR)
        val customColorHex = if (customColorIdx != -1 && !cursor.isNull(customColorIdx)) cursor.getString(customColorIdx) else null
        val taskDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
        val createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))

        val reminderIdx = cursor.getColumnIndex(COLUMN_REMINDER_MINUTES)
        val reminderMinutes = if (reminderIdx != -1 && !cursor.isNull(reminderIdx)) cursor.getInt(reminderIdx) else null

        return Task(
            id = id,
            title = title,
            subtitle = subtitle,
            startTime = startTime,
            endTime = endTime,
            status = TaskStatus.fromString(statusStr),
            progress = progress,
            colorTheme = TaskColorTheme.fromString(colorThemeStr),
            customColorHex = customColorHex,
            date = taskDate,
            reminderMinutesBefore = reminderMinutes,
            createdAt = createdAt
        )
    }

    fun getTaskById(id: Long): Task? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TASKS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToTask(it)
            }
        }
        return null
    }

    fun getTasksForDate(date: String): List<Task> {
        val taskList = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TASKS,
            null,
            "$COLUMN_DATE = ?",
            arrayOf(date),
            null,
            null,
            "$COLUMN_ID ASC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    taskList.add(cursorToTask(it))
                } while (it.moveToNext())
            }
        }
        return taskList
    }

    fun getAllTasks(): List<Task> {
        val taskList = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.query(TABLE_TASKS, null, null, null, null, null, "$COLUMN_DATE ASC, $COLUMN_ID ASC")
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    taskList.add(cursorToTask(it))
                } while (it.moveToNext())
            }
        }
        return taskList
    }
}
