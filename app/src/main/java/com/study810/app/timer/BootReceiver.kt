package com.study810.app.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.study810.app.Study810Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Restores the foreground service after a reboot when the timer was still
 * running. Failures are ignored: the persisted start time is untouched, so the
 * next app launch can restore the count anyway.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository =
                    (appContext as Study810Application).container.repository
                if (repository.isRunning()) {
                    StudyTimerService.resume(appContext)
                }
            } catch (_: Exception) {
                // Some Android versions restrict starting a special-use FGS
                // from BOOT_COMPLETED. The timer state is still persisted.
            } finally {
                pendingResult.finish()
            }
        }
    }
}
