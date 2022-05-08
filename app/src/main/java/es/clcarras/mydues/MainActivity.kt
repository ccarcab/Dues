package es.clcarras.mydues

import android.os.Bundle
import android.view.Menu
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.setupWithNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import es.clcarras.mydues.databinding.ActivityMainBinding
import es.clcarras.mydues.service.DuesNotificationWorker
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

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

    fun createWorkRequest(message: String, timeDelayInDays: Long): UUID {
        val myWorkRequest = OneTimeWorkRequestBuilder<DuesNotificationWorker>()
            .setInitialDelay(timeDelayInDays, TimeUnit.DAYS)
            .setInputData(
                workDataOf(
                    "title" to "Dues",
                    "message" to message,
                )
            )
            .build()

        WorkManager.getInstance(this).enqueue(myWorkRequest)
        // UUID usado en caso de que se quiera eliminar la notificación
        return myWorkRequest.id
    }

    fun deleteWork(uuid: UUID) {
        WorkManager.getInstance(this).cancelWorkById(uuid)
    }
}