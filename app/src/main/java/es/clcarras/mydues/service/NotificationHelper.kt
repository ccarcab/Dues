package es.clcarras.mydues.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R

const val CHANNEL_ID = "Dues Notification Channel"
const val CHANNEL_DESC = "Channel to alert the user the dues billing period is close."
const val NOTIFICATION_ID = 852022

class NotificationHelper(
    private val context: Context
) {

    fun createNotification(title: String, message: String) {

        createNotificationChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_menu_24)
            setContentTitle(title)
            setContentText(message)
            setContentIntent(pendingIntent)
            priority = NotificationCompat.PRIORITY_HIGH
            setVibrate(LongArray(0))
        }.build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)

    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_ID,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESC
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }

}