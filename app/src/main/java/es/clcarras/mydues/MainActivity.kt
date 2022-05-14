package es.clcarras.mydues

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import es.clcarras.mydues.databinding.ActivityMainBinding
import es.clcarras.mydues.service.DuesNotificationWorker
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        const val STANDARD_TIME_MARGIN = 24
        const val SHORT_TIME_MARGIN = 12
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

    fun createWorkRequest(message: String, delayInHours: Long): UUID {
        val delay =
            if (delayInHours <= 24) delayInHours - SHORT_TIME_MARGIN
            else delayInHours - STANDARD_TIME_MARGIN

        val myWorkRequest = PeriodicWorkRequestBuilder<DuesNotificationWorker>(
            delay, TimeUnit.HOURS,
            15, TimeUnit.MINUTES
        )
            .setInitialDelay(delay, TimeUnit.HOURS)
            .setInputData(
                workDataOf(
                    "title" to "Dues",
                    "message" to message
                )
            )
            .build()

        Log.i("WorkManager", "Delay in hours: $delay")

        WorkManager.getInstance(this).enqueue(myWorkRequest)
        // UUID usado en caso de que se quiera eliminar la notificación
        return myWorkRequest.id
    }

    fun deleteWork(uuid: UUID) {
        Log.i("WorkManager", "Deleted work: $uuid")
        WorkManager.getInstance(this).cancelWorkById(uuid)
    }
}