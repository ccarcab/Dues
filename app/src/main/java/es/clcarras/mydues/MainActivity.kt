package es.clcarras.mydues

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import es.clcarras.mydues.databinding.ActivityMainBinding
import es.clcarras.mydues.service.DuesNotificationWorker
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        const val GRACE_PERIOD = 24
    }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)
            setSupportActionBar(bottomAppBar)
        }
    }

    fun getBottomAppBar() = binding.bottomAppBar
    fun getFab() = binding.fab

    fun createWorkRequest(message: String, periodicityInHours: Long, delayInHours: Long): UUID {

        val myWorkRequest = PeriodicWorkRequestBuilder<DuesNotificationWorker>(
            periodicityInHours, TimeUnit.HOURS,
            5, TimeUnit.MINUTES
        ).apply {
            if (delayInHours - GRACE_PERIOD > 0)
                setInitialDelay(delayInHours - GRACE_PERIOD, TimeUnit.HOURS)

            setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            setInputData(workDataOf("title" to "Dues", "message" to message))
        }.build()

        Log.i("WorkManager", "Periodicity in hours: $periodicityInHours")
        Log.i("WorkManager", "Delay in hours from now: ${delayInHours - GRACE_PERIOD}")

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "${myWorkRequest.id}",
            ExistingPeriodicWorkPolicy.KEEP,
            myWorkRequest
        )
        // UUID usado en caso de que se quiera eliminar la notificación
        return myWorkRequest.id
    }

    fun deleteWork(uuid: UUID) {
        Log.i("WorkManager", "Deleted work: $uuid")
        WorkManager.getInstance(this).apply {
            cancelUniqueWork(uuid.toString())
            pruneWork()
        }
    }
}