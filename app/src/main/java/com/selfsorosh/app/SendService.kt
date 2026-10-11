package com.selfsorosh.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Handler
import android.os.Looper

class SendService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    private val task = object : Runnable {
        override fun run() {
            if (!running) return

            // محل اجرای کار زمان‌بندی‌شده.
            // ارسال پیام واقعی هنوز متصل نشده است.

            handler.postDelayed(this, intervalMs)
        }
    }

    private var intervalMs = 20_000L
    private var destination = ""
    private var messageText = ""

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "self_sorosh_service",
                "Self SoRosh",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        if (intent?.action == "STOP") {
            stopWork()
            stopSelf()
            return START_NOT_STICKY
        }

        destination = intent?.getStringExtra("destination") ?: destination
        messageText = intent?.getStringExtra("message") ?: messageText

        intervalMs = (intent?.getLongExtra("interval_ms", 20_000L)
            ?: 20_000L).coerceAtLeast(1_000L)

        val notification: Notification =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, "self_sorosh_service")
                    .setContentTitle("Self SoRosh")
                    .setContentText("سرویس فعال است")
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .setOngoing(true)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(this)
                    .setContentTitle("Self SoRosh")
                    .setContentText("سرویس فعال است")
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .setOngoing(true)
                    .build()
            }

        startForeground(1001, notification)

        if (!running) {
            running = true
            handler.post(task)
        }

        return START_NOT_STICKY
    }

    private fun stopWork() {
        running = false
        handler.removeCallbacks(task)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        stopWork()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
