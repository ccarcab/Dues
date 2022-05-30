package es.clcarras.mydues.service

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Worker usado para mostrar notificaciones al usuario
 */
class DuesNotificationWorker(
    private val context: Context, // Contexto usado para crear la notificación
    params: WorkerParameters // Parámetros del worker
) : Worker(context, params) {
    override fun doWork(): Result {

        // Se crea una notificación con los datos del worker
        NotificationHelper(context).createNotification(
            inputData.getString("title").toString(),
            inputData.getString("message").toString()
        )

        return Result.success()
    }
}