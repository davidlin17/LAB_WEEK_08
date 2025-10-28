package com.example.lab_week_08

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NotificationService : Service() {

    // Notification builder for displaying the notification
    private lateinit var notificationBuilder: NotificationCompat.Builder

    // Handler to control execution thread
    private lateinit var serviceHandler: Handler

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        // Create the notification with all of its configurations
        notificationBuilder = startForegroundService()

        // Create handler and thread for background execution
        val handlerThread = HandlerThread("SecondThread").apply { start() }
        serviceHandler = Handler(handlerThread.looper)
    }

    // Create the notification with its contents and configuration
    private fun startForegroundService(): NotificationCompat.Builder {
        val pendingIntent = getPendingIntent()
        val channelId = createNotificationChannel()
        val notificationBuilder = getNotificationBuilder(pendingIntent, channelId)
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        return notificationBuilder
    }

    // Creates a pending intent that launches MainActivity when notification is clicked
    private fun getPendingIntent(): PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), flag
        )
    }

    // Creates the notification channel (required for Android O and above)
    private fun createNotificationChannel(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "001"
            val channelName = "001 Channel"
            val channelPriority = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, channelPriority)
            val service = requireNotNull(
                ContextCompat.getSystemService(this, NotificationManager::class.java)
            )
            service.createNotificationChannel(channel)
            channelId
        } else { "" }

    // Builds the notification content
    private fun getNotificationBuilder(pendingIntent: PendingIntent, channelId: String) =
        NotificationCompat.Builder(this, channelId)
            .setContentTitle("Second worker process is done")
            .setContentText("Check it out!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setTicker("Second worker process is done, check it out!")
            .setOngoing(true)

    // 🔹 Tambahkan kode berikut (sesuai instruksi kamu)
    // This is a callback and part of a life cycle
    // This callback will be called when the service is started
    // after the startForeground() method is called in startForegroundService()
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val returnValue = super.onStartCommand(intent, flags, startId)

        // Gets the channel id passed from MainActivity through the Intent
        val Id = intent?.getStringExtra(EXTRA_ID)
            ?: throw IllegalStateException("Channel ID must be provided")

        // Posts the notification task to the handler, executed on a different thread
        serviceHandler.post {
            // Sets up what happens after the notification is posted
            countDownFromTenToZero(notificationBuilder)

            // Notify MainActivity that the service process is done
            notifyCompletion(Id)

            // Stop the foreground service and remove the notification
            stopForeground(STOP_FOREGROUND_REMOVE)

            // Stop and destroy the service
            stopSelf()
        }

        return returnValue
    }

    // 🔹 Function to update notification displaying a countdown 10 → 0
    private fun countDownFromTenToZero(notificationBuilder: NotificationCompat.Builder) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        for (i in 10 downTo 0) {
            Thread.sleep(1000L)
            notificationBuilder
                .setContentText("$i seconds until last warning")
                .setSilent(true)
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    // 🔹 Function to update LiveData with the returned channel id via Main Thread
    private fun notifyCompletion(Id: String) {
        Handler(Looper.getMainLooper()).post {
            mutableID.value = Id
        }
    }

    companion object {
        const val NOTIFICATION_ID = 0xCA7
        const val EXTRA_ID = "Id"

        // LiveData to track completion of the service
        private val mutableID = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableID
    }
}
