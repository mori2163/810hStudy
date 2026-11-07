package com.study810.app.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.study810.app.MainActivity
import com.study810.app.R
import com.study810.app.Study810Application
import com.study810.app.data.StudyRepository
import com.study810.app.util.TARGET_MILLIS
import com.study810.app.util.formatDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Keeps the study timer alive while the screen is off or the app is in the
 * background. The actual start time lives in DataStore, so the count is
 * restored even if this service is recreated.
 */
class StudyTimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var repository: StudyRepository
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        repository = (application as Study810Application).container.repository
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopAndFinish()

            ACTION_START -> {
                // Call startForeground immediately; DataStore/DB work happens next.
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(System.currentTimeMillis(), completedMillis = null),
                )
                scope.launch {
                    val start = repository.startTimer()
                    val completed = repository.totalCompletedMillis()
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        buildNotification(start, completedMillis = completed),
                    )
                }
            }

            else -> {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(System.currentTimeMillis(), completedMillis = null),
                )
                scope.launch {
                    val info = repository.timerInfo.first()
                    if (!info.isRunning) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    } else {
                        val completed = repository.totalCompletedMillis()
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            buildNotification(info.startEpochMillis, completedMillis = completed),
                        )
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun stopAndFinish() {
        scope.launch {
            repository.stopTimer()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun buildNotification(
        startEpochMillis: Long,
        completedMillis: Long?,
    ): Notification {
        val elapsed = (System.currentTimeMillis() - startEpochMillis).coerceAtLeast(0L)
        val text = if (completedMillis == null) {
            getString(R.string.notification_running)
        } else {
            val remaining = (TARGET_MILLIS - completedMillis - elapsed).coerceAtLeast(0L)
            getString(R.string.notification_remaining, formatDuration(remaining))
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(text)
            .setWhen(startEpochMillis)
            .setUsesChronometer(true)
            .setShowWhen(true)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(contentIntent())
            .addAction(
                R.drawable.ic_stat_timer,
                getString(R.string.notification_stop),
                stopIntent(),
            )
            .build()
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            REQUEST_CONTENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun stopIntent(): PendingIntent {
        val intent = Intent(this, StudyTimerService::class.java).setAction(ACTION_STOP)
        return PendingIntent.getService(
            this,
            REQUEST_STOP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.study810.app.action.START_TIMER"
        const val ACTION_STOP = "com.study810.app.action.STOP_TIMER"
        const val ACTION_RESUME = "com.study810.app.action.RESUME_TIMER"

        private const val CHANNEL_ID = "study_timer"
        private const val NOTIFICATION_ID = 810
        private const val REQUEST_CONTENT = 1
        private const val REQUEST_STOP = 2

        fun start(context: Context) {
            val intent = Intent(context, StudyTimerService::class.java)
                .setAction(ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, StudyTimerService::class.java)
                .setAction(ACTION_STOP)
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, StudyTimerService::class.java)
                .setAction(ACTION_RESUME)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
