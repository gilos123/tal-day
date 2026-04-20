package com.maayan.studytracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StudyTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createTimerNotificationChannel()
    }

    private fun createTimerNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                TIMER_CHANNEL_ID,
                "Study Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing study session countdown"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val TIMER_CHANNEL_ID = "study_timer_channel"
    }
}
