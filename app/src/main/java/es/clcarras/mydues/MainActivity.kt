package es.clcarras.mydues

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import es.clcarras.mydues.databinding.ActivityMainBinding
import es.clcarras.mydues.service.DuesNotificationWorker
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        const val TIME_MARGIN = 24
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bottom_app_bar, menu)
        return true
    }

    fun createWorkRequest(message: String, delayInHours: Long): UUID {
        val myWorkRequest = OneTimeWorkRequestBuilder<DuesNotificationWorker>()
            .setInitialDelay(delayInHours - TIME_MARGIN, TimeUnit.HOURS)
            .setInputData(
                workDataOf(
                    "title" to "Dues",
                    "message" to message
                )
            )
            .build()

        Log.i("createWorkRequest", "Delay in hours $delayInHours")

        WorkManager.getInstance(this).enqueue(myWorkRequest)
        // UUID usado en caso de que se quiera eliminar la notificación
        return myWorkRequest.id
    }

    fun deleteWork(uuid: UUID) {
        WorkManager.getInstance(this).cancelWorkById(uuid)
    }
}