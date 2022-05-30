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
import es.clcarras.mydues.constants.CHANNEL_DESC
import es.clcarras.mydues.constants.CHANNEL_ID
import es.clcarras.mydues.constants.NOTIFICATION_ID

/**
 * Clase usada para crear y mostrar una notificación
 */
class NotificationHelper(private val context: Context) {

    /**
     * Método que crea y muestra una notificación con el título y el mensaje recibidos
     */
    fun createNotification(title: String, message: String) {

        // Crea el canal de notificaciones
        createNotificationChannel()

        // Intent para abrir la aplicación al pulsar la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Pending Intent que se asociará a la notificación
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Se crea la notificación mediante el builder
        val notification = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle(title)
            setContentText(message)
            setContentIntent(pendingIntent)
            priority = NotificationCompat.PRIORITY_HIGH
            setVibrate(LongArray(0))
        }.build()

        // Se muestra la notificación mediante el manager
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    /**
     * Método usado para crear el canal de notificaciones
     */
    private fun createNotificationChannel() {

        // Canal de notificaiones
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_ID,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESC
        }

        // Se añade el canal de notificaciones creado mediante el notification manager
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
            createNotificationChannel(channel)
        }
    }

}