package com.study810.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One finished study session. [durationMillis] is stored explicitly so that
 * daylight-saving changes and clock adjustments cannot silently change history.
 */
@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val durationMillis: Long,
)
