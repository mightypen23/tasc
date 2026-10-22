package com.mightypen.tasc.data

import android.content.Context
import android.content.SharedPreferences
import com.mightypen.tasc.ui.theme.AppThemes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TaskCardSizePreset(val label: String, val heightDp: Int) {
    COMPACT("Compact", 94),
    STANDARD("Standard", 114),
    SPACIOUS("Spacious", 138),
    CUSTOM("Custom", 114);

    companion object {
        fun fromHeight(height: Int): TaskCardSizePreset {
            return entries.firstOrNull { it != CUSTOM && it.heightDp == height } ?: CUSTOM
        }
    }
}

class TaskPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("tasc_user_prefs", Context.MODE_PRIVATE)

    private val _cardSizePreset = MutableStateFlow(
        try {
            TaskCardSizePreset.valueOf(
                prefs.getString(KEY_SIZE_PRESET, TaskCardSizePreset.STANDARD.name)
                    ?: TaskCardSizePreset.STANDARD.name
            )
        } catch (e: Exception) {
            TaskCardSizePreset.STANDARD
        }
    )
    val cardSizePreset: StateFlow<TaskCardSizePreset> = _cardSizePreset.asStateFlow()

    private val _cardHeightDp = MutableStateFlow(
        prefs.getInt(KEY_CARD_HEIGHT, TaskCardSizePreset.STANDARD.heightDp)
    )
    val cardHeightDp: StateFlow<Int> = _cardHeightDp.asStateFlow()

    private val _appThemeId = MutableStateFlow(
        prefs.getString(KEY_APP_THEME_ID, AppThemes.CYBER_LIME.id) ?: AppThemes.CYBER_LIME.id
    )
    val appThemeId: StateFlow<String> = _appThemeId.asStateFlow()

    // Default starter custom colors curated for modern UI
    private val defaultStarterColors = listOf(
        "#FF6B6B", // Coral Red
        "#4ECDC4", // Teal / Turquoise
        "#A8E6CF", // Soft Mint
        "#FD79A8", // Flamingo Pink
        "#A29BFE", // Soft Periwinkle
        "#FFA502"  // Warm Amber
    )

    private val _customColors = MutableStateFlow(loadCustomColors())
    val customColors: StateFlow<List<String>> = _customColors.asStateFlow()

    private fun loadCustomColors(): List<String> {
        val saved = prefs.getString(KEY_CUSTOM_COLORS, null)
        return if (!saved.isNullOrBlank()) {
            saved.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            defaultStarterColors
        }
    }

    fun saveAllSettings(preset: TaskCardSizePreset, heightDp: Int, themeId: String) {
        val clampedHeight = heightDp.coerceIn(84, 150)
        _cardSizePreset.value = preset
        _cardHeightDp.value = clampedHeight
        _appThemeId.value = themeId

        prefs.edit()
            .putString(KEY_SIZE_PRESET, preset.name)
            .putInt(KEY_CARD_HEIGHT, clampedHeight)
            .putString(KEY_APP_THEME_ID, themeId)
            .apply()
    }

    fun setCardSizePreset(preset: TaskCardSizePreset) {
        _cardSizePreset.value = preset
        _cardHeightDp.value = preset.heightDp
        prefs.edit()
            .putString(KEY_SIZE_PRESET, preset.name)
            .putInt(KEY_CARD_HEIGHT, preset.heightDp)
            .apply()
    }

    fun setCustomCardHeight(heightDp: Int) {
        val clamped = heightDp.coerceIn(84, 150)
        _cardHeightDp.value = clamped
        _cardSizePreset.value = TaskCardSizePreset.fromHeight(clamped)
        prefs.edit()
            .putString(KEY_SIZE_PRESET, _cardSizePreset.value.name)
            .putInt(KEY_CARD_HEIGHT, clamped)
            .apply()
    }

    fun setAppThemeId(themeId: String) {
        _appThemeId.value = themeId
        prefs.edit()
            .putString(KEY_APP_THEME_ID, themeId)
            .apply()
    }

    fun addCustomColor(hexCode: String): Boolean {
        var cleanHex = hexCode.trim().uppercase()
        if (!cleanHex.startsWith("#")) {
            cleanHex = "#$cleanHex"
        }
        // Basic hex format validation: #RRGGBB or #AARRGGBB
        if (!cleanHex.matches(Regex("^#([A-F0-9]{6}|[A-F0-9]{8})$"))) {
            return false
        }

        val current = _customColors.value.toMutableList()
        if (!current.contains(cleanHex)) {
            current.add(cleanHex)
            _customColors.value = current.toList()
            prefs.edit().putString(KEY_CUSTOM_COLORS, current.joinToString(",")).apply()
            return true
        }
        return false
    }

    fun removeCustomColor(hexCode: String) {
        val current = _customColors.value.toMutableList()
        if (current.remove(hexCode)) {
            _customColors.value = current.toList()
            prefs.edit().putString(KEY_CUSTOM_COLORS, current.joinToString(",")).apply()
        }
    }

    private val defaultCategories = listOf(
        "Work", "Design", "Personal", "Health", "Study", "Meeting"
    )

    private val _categories = MutableStateFlow(loadCategories())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private fun loadCategories(): List<String> {
        val saved = prefs.getString(KEY_CATEGORIES, null)
        return if (!saved.isNullOrBlank()) {
            saved.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            defaultCategories
        }
    }

    fun addCategory(name: String): Boolean {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return false
        val current = _categories.value.toMutableList()
        val exists = current.any { it.equals(cleanName, ignoreCase = true) }
        if (!exists) {
            current.add(cleanName)
            _categories.value = current.toList()
            prefs.edit().putString(KEY_CATEGORIES, current.joinToString(",")).apply()
            return true
        }
        return false
    }

    fun removeCategory(name: String) {
        val current = _categories.value.toMutableList()
        val removed = current.removeAll { it.equals(name.trim(), ignoreCase = true) }
        if (removed) {
            _categories.value = current.toList()
            prefs.edit().putString(KEY_CATEGORIES, current.joinToString(",")).apply()
        }
    }

    companion object {
        private const val KEY_SIZE_PRESET = "key_card_size_preset"
        private const val KEY_CARD_HEIGHT = "key_card_height_dp"
        private const val KEY_APP_THEME_ID = "key_app_theme_id"
        private const val KEY_CUSTOM_COLORS = "key_custom_colors_list"
        private const val KEY_CATEGORIES = "key_custom_categories_list"
    }
}
