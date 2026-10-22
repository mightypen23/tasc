package com.mightypen.tasc.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SelectedDatePill,
    onPrimary = SelectedDateText,
    secondary = CardCyanBg,
    onSecondary = CardCyanText,
    tertiary = CardLavenderBg,
    onTertiary = CardLavenderText,
    background = AppBackgroundDark,
    onBackground = androidx.compose.ui.graphics.Color.White,
    surface = AppBackgroundTop,
    onSurface = androidx.compose.ui.graphics.Color.White
)

@Composable
fun TascTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = AppBackgroundDark.toArgb()
                window.navigationBarColor = AppBackgroundDark.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
