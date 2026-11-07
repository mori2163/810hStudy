package com.study810.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Query("SELECT * FROM study_sessions ORDER BY startEpochMillis DESC")
    fun observeAll(): Flow<List<StudySession>>

    @Insert
    suspend fun insert(session: StudySession): Long

    @Query("SELECT COALESCE(SUM(durationMillis), 0) FROM study_sessions")
    suspend fun getTotalMillis(): Long
}
