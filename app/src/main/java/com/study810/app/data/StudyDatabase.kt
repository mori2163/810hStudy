package com.study810.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [StudySession::class],
    version = 1,
    exportSchema = false,
)
abstract class StudyDatabase : RoomDatabase() {

    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile
        private var instance: StudyDatabase? = null

        fun getInstance(context: Context): StudyDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "study810.db",
                ).build().also { instance = it }
            }
        }
    }
}
