package com.mightypen.tasc

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mightypen.tasc.ui.TaskViewModel
import com.mightypen.tasc.ui.screens.TaskScreen
import com.mightypen.tasc.ui.theme.AppBackgroundDark
import com.mightypen.tasc.ui.theme.TascTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Permission result handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create high-priority notification channel for task reminders
        NotificationHelper.createNotificationChannel(this)

        // Request POST_NOTIFICATIONS on Android 13+ (API 33+) if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Set status bar icons to light (white) to match our dark navy theme
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false

        setContent {
            TascTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppBackgroundDark
                ) {
                    val taskViewModel: TaskViewModel = viewModel()
                    TaskScreen(viewModel = taskViewModel)
                }
            }
        }
    }
}
