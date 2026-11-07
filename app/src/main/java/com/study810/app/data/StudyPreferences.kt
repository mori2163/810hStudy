package com.study810.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.studyDataStore by preferencesDataStore(name = "study_prefs")

data class TimerInfo(
    val isRunning: Boolean,
    val startEpochMillis: Long,
)

class StudyPreferences(private val context: Context) {

    private object Keys {
        val RUNNING_START = longPreferencesKey("running_start_epoch_millis")
        val FIRST_LAUNCH_EPOCH_DAY = longPreferencesKey("first_launch_epoch_day")
    }

    private val data = context.studyDataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val timerInfo: Flow<TimerInfo> = data.map { preferences ->
        val start = preferences[Keys.RUNNING_START] ?: 0L
        TimerInfo(isRunning = start > 0L, startEpochMillis = start)
    }

    val firstLaunchEpochDay: Flow<Long?> = data.map { preferences ->
        preferences[Keys.FIRST_LAUNCH_EPOCH_DAY]
    }

    suspend fun ensureFirstLaunch(epochDay: Long) {
        context.studyDataStore.edit { preferences ->
            if (preferences[Keys.FIRST_LAUNCH_EPOCH_DAY] == null) {
                preferences[Keys.FIRST_LAUNCH_EPOCH_DAY] = epochDay
            }
        }
    }

    suspend fun startTimer(startEpochMillis: Long) {
        context.studyDataStore.edit { preferences ->
            preferences[Keys.RUNNING_START] = startEpochMillis
        }
    }

    /**
     * Clears the running marker and returns the start time that was stored.
     * Returns null when the timer was not running.
     */
    suspend fun clearTimer(): Long? {
        var start: Long? = null
        context.studyDataStore.edit { preferences ->
            val stored = preferences[Keys.RUNNING_START] ?: 0L
            if (stored > 0L) {
                start = stored
                preferences.remove(Keys.RUNNING_START)
            }
        }
        return start
    }
}
