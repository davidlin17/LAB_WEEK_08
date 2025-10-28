package com.example.lab_week_08

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.example.lab_week_08.R

class SecondNotificationService : Service() {

    companion object {
        val trackingCompletion = MutableLiveData<String>()
        const val CHANNEL_ID = "SECOND_CHANNEL_ID"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val idValue = intent?.getStringExtra(EXTRA_ID) ?: "002"

        createNotificationChannel()
        startForeground(2, getNotificationBuilder(idValue))

        Thread {
            // simulasi 5 detik proses
            Thread.sleep(5000L)
            trackingCompletion.postValue(idValue)
            stopSelf()
        }.start()

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Second Foreground Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun getNotificationBuilder(idValue: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Second Foreground Service Running")
            .setContentText("Processing Notification ID $idValue")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val EXTRA_ID = "Id"
    }
}
