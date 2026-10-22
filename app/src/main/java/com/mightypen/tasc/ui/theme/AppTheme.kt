package com.mightypen.tasc.ui.theme

import androidx.compose.ui.graphics.Color

data class AppThemePair(
    val id: String,
    val name: String,
    val backgroundTop: Color,
    val backgroundDark: Color,
    val backgroundBottom: Color,
    val accentButton: Color,
    val onAccentButton: Color,
    val domeArcColor: Color = Color(0xFF233651)
)

object AppThemes {
    val CYBER_LIME = AppThemePair(
        id = "CYBER_LIME",
        name = "Cyber Lime",
        backgroundTop = Color(0xFF172438),
        backgroundDark = Color(0xFF131E2F),
        backgroundBottom = Color(0xFF0F1725),
        accentButton = Color(0xFFD6F58F),
        onAccentButton = Color(0xFF101D24),
        domeArcColor = Color(0xFF233651)
    )

    val ELECTRIC_CYAN = AppThemePair(
        id = "ELECTRIC_CYAN",
        name = "Electric Cyan",
        backgroundTop = Color(0xFF101B2E),
        backgroundDark = Color(0xFF0B1424),
        backgroundBottom = Color(0xFF060B14),
        accentButton = Color(0xFF00E5FF),
        onAccentButton = Color(0xFF032233),
        domeArcColor = Color(0xFF18314C)
    )

    val SUNSET_CORAL = AppThemePair(
        id = "SUNSET_CORAL",
        name = "Sunset Coral",
        backgroundTop = Color(0xFF261828),
        backgroundDark = Color(0xFF1B111D),
        backgroundBottom = Color(0xFF120A14),
        accentButton = Color(0xFFFF6E85),
        onAccentButton = Color(0xFF330913),
        domeArcColor = Color(0xFF45223C)
    )

    val AMETHYST_GLOW = AppThemePair(
        id = "AMETHYST_GLOW",
        name = "Amethyst Glow",
        backgroundTop = Color(0xFF201B3E),
        backgroundDark = Color(0xFF15122B),
        backgroundBottom = Color(0xFF0D0A1E),
        accentButton = Color(0xFFC084FC),
        onAccentButton = Color(0xFF24073D),
        domeArcColor = Color(0xFF3B285E)
    )

    val EMERALD_FOREST = AppThemePair(
        id = "EMERALD_FOREST",
        name = "Emerald Forest",
        backgroundTop = Color(0xFF122822),
        backgroundDark = Color(0xFF0C1B17),
        backgroundBottom = Color(0xFF06100D),
        accentButton = Color(0xFF34D399),
        onAccentButton = Color(0xFF052B1E),
        domeArcColor = Color(0xFF1B4237)
    )

    val SOLAR_AMBER = AppThemePair(
        id = "SOLAR_AMBER",
        name = "Solar Amber",
        backgroundTop = Color(0xFF262118),
        backgroundDark = Color(0xFF1C1811),
        backgroundBottom = Color(0xFF110E08),
        accentButton = Color(0xFFFBBF24),
        onAccentButton = Color(0xFF332000),
        domeArcColor = Color(0xFF473A21)
    )

    val allThemes = listOf(
        CYBER_LIME,
        ELECTRIC_CYAN,
        SUNSET_CORAL,
        AMETHYST_GLOW,
        EMERALD_FOREST,
        SOLAR_AMBER
    )

    fun fromId(id: String): AppThemePair {
        return allThemes.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CYBER_LIME
    }
}
