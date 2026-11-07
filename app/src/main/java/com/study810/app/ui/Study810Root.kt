package com.study810.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.study810.app.R
import com.study810.app.timer.StudyTimerService

@Composable
fun Study810Root(viewModel: StudyViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        // Even if the user declines notifications, the foreground service can
        // keep measuring; Android just hides the notification entry.
        StudyTimerService.start(context)
    }

    val startTimer = {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            StudyTimerService.start(context)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink),
    ) {
        when (tab) {
            0 -> TimerScreen(
                state = state,
                onStart = startTimer,
                onStop = { StudyTimerService.stop(context) },
            )

            else -> CalendarScreen(
                sessions = sessions,
                runningStartEpochMillis = state.runningStartEpochMillis,
            )
        }

        AppBottomBar(
            selectedTab = tab,
            onSelect = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun AppBottomBar(
    selectedTab: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = Paper.copy(alpha = 0.15f))
        NavigationBar(
            containerColor = Ink,
            tonalElevation = 0.dp,
        ) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { onSelect(0) },
                icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                label = { Text(stringResource(R.string.tab_timer)) },
                colors = navigationColors(),
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { onSelect(1) },
                icon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                label = { Text(stringResource(R.string.tab_calendar)) },
                colors = navigationColors(),
            )
        }
    }
}

@Composable
private fun navigationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Ink,
    selectedTextColor = Paper,
    indicatorColor = Paper,
    unselectedIconColor = Paper.copy(alpha = 0.55f),
    unselectedTextColor = Paper.copy(alpha = 0.55f),
)
