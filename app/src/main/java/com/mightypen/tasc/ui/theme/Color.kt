package com.mightypen.tasc.ui.theme

import androidx.compose.ui.graphics.Color
import com.mightypen.tasc.model.TaskColorTheme

// Core App Theme Colors
val AppBackgroundDark = Color(0xFF131E2F)
val AppBackgroundTop = Color(0xFF172438)
val AppBackgroundBottom = Color(0xFF0F1725)

// Date Pill Colors
val SelectedDatePill = Color(0xFFD4F698)
val SelectedDateText = Color(0xFF111D11)
val UnselectedDatePill = Color(0xFF182436)
val UnselectedDateNumber = Color(0xFF8FA0B9)
val UnselectedDateDay = Color(0xFF586C87)

// Timeline Colors
val TimelineTimeText = Color(0xFF7D91AC)
val TimelineDashedLine = Color(0xFF27384E)
val TimelineDividerLine = Color(0xFF1B283A)

// Card Color Palettes
val CardLimeBg = Color(0xFFD2F69A)
val CardLimeText = Color(0xFF132214)
val CardLimeSubtext = Color(0xFF38523A)

val CardCyanBg = Color(0xFF37C6EE)
val CardCyanText = Color(0xFF0B2A3D)
val CardCyanSubtext = Color(0xFF1A5676)

val CardLavenderBg = Color(0xFFD3C3E6)
val CardLavenderText = Color(0xFF2A1A38)
val CardLavenderSubtext = Color(0xFF513E65)

val CardYellowBg = Color(0xFFE5FA8E)
val CardYellowText = Color(0xFF232E14)
val CardYellowSubtext = Color(0xFF4C5E2D)

val CardCoralBg = Color(0xFFFFB3BA)
val CardCoralText = Color(0xFF381418)
val CardCoralSubtext = Color(0xFF6B2D34)

// Dark Squircle Icon container on cards
val CardIconContainer = Color(0xFF152233)

data class CardColorPalette(
    val background: Color,
    val text: Color,
    val subtext: Color,
    val iconColor: Color
)

fun getCardPalette(theme: TaskColorTheme = TaskColorTheme.LIME, customHex: String? = null): CardColorPalette {
    if (!customHex.isNullOrBlank()) {
        try {
            val clean = if (customHex.startsWith("#")) customHex else "#$customHex"
            val parsed = Color(android.graphics.Color.parseColor(clean))
            // Compute relative luminance
            val luminance = 0.299f * parsed.red + 0.587f * parsed.green + 0.114f * parsed.blue
            val isLight = luminance > 0.46f

            val textColor = if (isLight) Color(0xFF101B24) else Color.White
            val subtextColor = if (isLight) Color(0xFF2E3E4C) else Color.White.copy(alpha = 0.78f)

            return CardColorPalette(
                background = parsed,
                text = textColor,
                subtext = subtextColor,
                iconColor = parsed
            )
        } catch (e: Exception) {
            // fallback
        }
    }

    return when (theme) {
        TaskColorTheme.LIME -> CardColorPalette(CardLimeBg, CardLimeText, CardLimeSubtext, CardLimeBg)
        TaskColorTheme.CYAN -> CardColorPalette(CardCyanBg, CardCyanText, CardCyanSubtext, CardCyanBg)
        TaskColorTheme.LAVENDER -> CardColorPalette(CardLavenderBg, CardLavenderText, CardLavenderSubtext, CardLavenderBg)
        TaskColorTheme.YELLOW -> CardColorPalette(CardYellowBg, CardYellowText, CardYellowSubtext, CardYellowBg)
        TaskColorTheme.CORAL -> CardColorPalette(CardCoralBg, CardCoralText, CardCoralSubtext, CardCoralBg)
    }
}
