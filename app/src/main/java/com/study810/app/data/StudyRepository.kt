package com.study810.app.data

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StudyRepository(
    private val dao: StudySessionDao,
    private val preferences: StudyPreferences,
) {
    private val mutex = Mutex()

    val sessions: Flow<List<StudySession>> = dao.observeAll()
    val timerInfo: Flow<TimerInfo> = preferences.timerInfo
    val firstLaunchEpochDay: Flow<Long?> = preferences.firstLaunchEpochDay

    suspend fun ensureFirstLaunch() {
        preferences.ensureFirstLaunch(LocalDate.now().toEpochDay())
    }

    /** Starts the timer and returns the epoch millis the session began at. */
    suspend fun startTimer(): Long = mutex.withLock {
        val current = preferences.timerInfo.first()
        if (current.isRunning) {
            current.startEpochMillis
        } else {
            System.currentTimeMillis().also { preferences.startTimer(it) }
        }
    }

    /**
     * Stops the running timer and persists a session when at least one second
     * was measured. Returns the saved session, or null when there was nothing
     * meaningful to save.
     */
    suspend fun stopTimer(): StudySession? = mutex.withLock {
        val start = preferences.clearTimer() ?: return@withLock null
        val end = System.currentTimeMillis()
        val duration = (end - start).coerceAtLeast(0L)
        if (duration < 1_000L) {
            null
        } else {
            val session = StudySession(
                startEpochMillis = start,
                endEpochMillis = end,
                durationMillis = duration,
            )
            session.copy(id = dao.insert(session))
        }
    }

    suspend fun isRunning(): Boolean = preferences.timerInfo.first().isRunning

    suspend fun totalCompletedMillis(): Long = dao.getTotalMillis()
}
