package com.ritualfocus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ritualfocus.app.ui.AppViewModel
import com.ritualfocus.app.ui.RitualFocusApp
import com.ritualfocus.app.ui.resolveDark
import com.ritualfocus.app.ui.theme.RitualFocusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AppViewModel = viewModel()
            val data by viewModel.data.collectAsStateWithLifecycle()
            val dark = resolveDark(data.settings.theme)

            RitualFocusTheme(darkTheme = dark) {
                LaunchedEffect(dark) {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
                    }
                }
                RitualFocusApp(viewModel)
            }
        }
    }
}
