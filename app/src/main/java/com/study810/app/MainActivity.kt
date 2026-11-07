package com.study810.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.study810.app.timer.StudyTimerService
import com.study810.app.ui.Study810Root
import com.study810.app.ui.Study810Theme
import com.study810.app.ui.StudyViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        lifecycleScope.launch {
            val repository = (application as Study810Application).container.repository
            if (repository.isRunning()) {
                StudyTimerService.resume(this@MainActivity)
            }
        }

        setContent {
            Study810Theme {
                val viewModel: StudyViewModel = viewModel(factory = StudyViewModel.Factory)
                Study810Root(viewModel = viewModel)
            }
        }
    }
}
