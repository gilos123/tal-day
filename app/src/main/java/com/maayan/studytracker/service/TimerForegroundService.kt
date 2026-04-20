package com.maayan.studytracker.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.maayan.studytracker.MainActivity
import com.maayan.studytracker.StudyTrackerApp
import com.maayan.studytracker.data.repository.TimerRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that owns the authoritative countdown for the active study timer.
 * Survives backgrounding; writes a TimerSession on completion or stop.
 */
@AndroidEntryPoint
class TimerForegroundService : LifecycleService() {

    @Inject lateinit var stateHolder: TimerStateHolder
    @Inject lateinit var timerRepository: TimerRepository

    private var tickJob: Job? = null
    private var startedAtElapsed: Long = 0L
    private var plannedSeconds: Long = 0L
    private var scheduleItemId: Long = -1L
    private var topicName: String = ""
    private var date: String = ""

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {
                scheduleItemId = intent.getLongExtra(EXTRA_SCHEDULE_ITEM_ID, -1L)
                topicName = intent.getStringExtra(EXTRA_TOPIC_NAME).orEmpty()
                date = intent.getStringExtra(EXTRA_DATE).orEmpty()
                plannedSeconds = intent.getLongExtra(EXTRA_PLANNED_SECONDS, 0L)
                startCountdown()
            }
            ACTION_STOP -> {
                stopCountdown(completedNaturally = false)
            }
            else -> { /* ignore */ }
        }
        return START_NOT_STICKY
    }

    private fun startCountdown() {
        startedAtElapsed = System.currentTimeMillis()
        startForeground(NOTIFICATION_ID, buildNotification(plannedSeconds))
        stateHolder.update(
            TimerUiState(
                scheduleItemId = scheduleItemId,
                topicName = topicName,
                date = date,
                plannedSeconds = plannedSeconds,
                remainingSeconds = plannedSeconds,
                running = true,
                finished = false
            )
        )

        tickJob?.cancel()
        tickJob = lifecycleScope.launch {
            while (true) {
                delay(1_000L)
                val elapsed = (System.currentTimeMillis() - startedAtElapsed) / 1_000L
                val remaining = (plannedSeconds - elapsed).coerceAtLeast(0L)
                stateHolder.update(
                    stateHolder.state.value.copy(
                        remainingSeconds = remaining,
                        running = remaining > 0
                    )
                )
                updateNotification(remaining)
                if (remaining <= 0L) {
                    stopCountdown(completedNaturally = true)
                    break
                }
            }
        }
    }

    private fun stopCountdown(completedNaturally: Boolean) {
        tickJob?.cancel()
        tickJob = null

        val elapsed = ((System.currentTimeMillis() - startedAtElapsed) / 1_000L)
            .coerceAtLeast(0L)
            .coerceAtMost(plannedSeconds)
        val actual = if (completedNaturally) plannedSeconds else elapsed

        lifecycleScope.launch {
            timerRepository.recordSession(
                scheduleItemId = if (scheduleItemId <= 0) null else scheduleItemId,
                topicName = topicName,
                date = date,
                actualDurationSeconds = actual
            )
            stateHolder.update(
                stateHolder.state.value.copy(
                    remainingSeconds = 0,
                    running = false,
                    finished = true
                )
            )
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun buildNotification(remainingSeconds: Long): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, TimerForegroundService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, StudyTrackerApp.TIMER_CHANNEL_ID)
            .setContentTitle("Studying: ${topicName.ifBlank { "Session" }}")
            .setContentText(formatTime(remainingSeconds) + " remaining")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopIntent)
            .build()
    }

    private fun updateNotification(remainingSeconds: Long) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(remainingSeconds))
    }

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    companion object {
        const val ACTION_START = "com.maayan.studytracker.service.START"
        const val ACTION_STOP = "com.maayan.studytracker.service.STOP"

        const val EXTRA_SCHEDULE_ITEM_ID = "scheduleItemId"
        const val EXTRA_TOPIC_NAME = "topicName"
        const val EXTRA_DATE = "date"
        const val EXTRA_PLANNED_SECONDS = "plannedSeconds"

        private const val NOTIFICATION_ID = 101

        fun start(
            context: Context,
            scheduleItemId: Long,
            topicName: String,
            date: String,
            plannedSeconds: Long
        ) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SCHEDULE_ITEM_ID, scheduleItemId)
                putExtra(EXTRA_TOPIC_NAME, topicName)
                putExtra(EXTRA_DATE, date)
                putExtra(EXTRA_PLANNED_SECONDS, plannedSeconds)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
