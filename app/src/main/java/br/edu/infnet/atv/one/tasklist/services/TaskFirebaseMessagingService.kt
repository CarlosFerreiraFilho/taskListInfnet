package br.edu.infnet.atv.one.tasklist.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import br.edu.infnet.atv.one.tasklist.R
import br.edu.infnet.atv.one.tasklist.ui.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class TaskFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Mensagem de dados recebida: " + remoteMessage.data)
        }

        remoteMessage.notification?.let {
            Log.d("FCM", "Título: ${it.title}, Corpo: ${it.body}")
            sendNotification(it.title, it.body)
        }
    }

    private fun sendNotification(title: String?, body: String?) {
        val intent = Intent(this, MainActivity::class.java)

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

        val notification: Notification = NotificationCompat.Builder(this, "default")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.post_add)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notification)
    }


    private fun createNotificationChannel() {
        val channelId = "default"
        val channelName = "Default Channel"
        val channelDescription = "This is the default notification channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        Log.d("Notification", "Canal de notificação criado: $channelId")
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
}
