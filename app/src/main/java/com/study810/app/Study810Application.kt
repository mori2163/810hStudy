package com.study810.app

import android.app.Application
import android.content.Context
import com.study810.app.data.StudyDatabase
import com.study810.app.data.StudyPreferences
import com.study810.app.data.StudyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    private val database = StudyDatabase.getInstance(context)
    val repository = StudyRepository(
        dao = database.studySessionDao(),
        preferences = StudyPreferences(context.applicationContext),
    )
}

class Study810Application : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch { container.repository.ensureFirstLaunch() }
    }
}
