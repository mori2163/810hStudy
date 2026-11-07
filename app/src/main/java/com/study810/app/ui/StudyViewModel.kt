package com.study810.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.study810.app.Study810Application
import com.study810.app.data.StudyRepository
import com.study810.app.util.TARGET_MILLIS
import com.study810.app.util.overlapMillis
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

data class StudyUiState(
    val isRunning: Boolean = false,
    val runningStartEpochMillis: Long? = null,
    val currentSessionMillis: Long = 0L,
    val consumedMillis: Long = 0L,
    val remainingMillis: Long = TARGET_MILLIS,
    val todayMillis: Long = 0L,
    val firstLaunchEpochDay: Long? = null,
)

class StudyViewModel(
    private val repository: StudyRepository,
) : ViewModel() {

    val sessions = repository.sessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = emptyList(),
    )

    private val ticker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000L)
        }
    }

    val uiState: StateFlow<StudyUiState> = combine(
        repository.sessions,
        repository.timerInfo,
        repository.firstLaunchEpochDay,
        ticker,
    ) { sessions, timer, firstLaunchEpochDay, now ->
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val runningStart = timer.startEpochMillis.takeIf { timer.isRunning }
        val currentSession = runningStart
            ?.let { (now - it).coerceAtLeast(0L) }
            ?: 0L
        val completed = sessions.sumOf { it.durationMillis }
        val consumed = (completed + currentSession).coerceIn(0L, TARGET_MILLIS)

        val todayCompleted = sessions.sumOf {
            overlapMillis(it.startEpochMillis, it.endEpochMillis, dayStart, dayEnd)
        }
        val todayRunning = runningStart
            ?.let { overlapMillis(it, now, dayStart, dayEnd) }
            ?: 0L

        StudyUiState(
            isRunning = timer.isRunning,
            runningStartEpochMillis = runningStart,
            currentSessionMillis = currentSession,
            consumedMillis = consumed,
            remainingMillis = (TARGET_MILLIS - consumed).coerceAtLeast(0L),
            todayMillis = todayCompleted + todayRunning,
            firstLaunchEpochDay = firstLaunchEpochDay,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = StudyUiState(),
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as Study810Application
                StudyViewModel(application.container.repository)
            }
        }
    }
}
