package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FarmCareApp
import com.example.ui.theme.FarmCareTheme
import com.example.ui.viewmodel.FarmViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val farmViewModel: FarmViewModel = viewModel()
            val settings by farmViewModel.settings.collectAsState()
            FarmCareTheme(
                themeMode = settings.themeMode,
                largeText = settings.largeText
            ) {
                FarmCareApp(viewModel = farmViewModel)
            }
        }
    }
}
