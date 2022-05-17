package es.clcarras.mydues

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.work.*
import es.clcarras.mydues.databinding.ActivityMainBinding
import es.clcarras.mydues.service.DuesNotificationWorker
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        // One day in millis
        const val GRACE_PERIOD = 86400000L
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var nav: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Dues)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)
            setSupportActionBar(bottomAppBar)
        }
        nav = findNavController(R.id.nav_host_fragment)
    }

    fun getBottomAppBar() = binding.bottomAppBar

    fun getFab() = binding.fab

    fun createWorkRequestPrueba() {

        val workName = "createWorkRequestPrueba.Test"

        WorkManager.getInstance(applicationContext).cancelUniqueWork(workName)

        val minutes = 30L
        val periodicityInMillis = minutes * 60000

        val myWorkRequest = PeriodicWorkRequestBuilder<DuesNotificationWorker>(
            periodicityInMillis, TimeUnit.MILLISECONDS
        ).apply {

            setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            setInputData(
                workDataOf(
                    "title" to "Dues",
                    "message" to "Testing Worker in $minutes minutes!"
                )
            )
        }.build()

        Log.i("WorkManager", "Periodicity in millis: $periodicityInMillis")

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.KEEP,
            myWorkRequest
        )
    }

    fun createWorkRequest(message: String, periodicityInMillis: Long, delayInMillis: Long): UUID {

        val myWorkRequest = PeriodicWorkRequestBuilder<DuesNotificationWorker>(
            periodicityInMillis, TimeUnit.MILLISECONDS
        ).apply {
            if (delayInMillis - GRACE_PERIOD > 0)
                setInitialDelay(delayInMillis - GRACE_PERIOD, TimeUnit.MILLISECONDS)

            setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            setInputData(
                workDataOf(
                    "title" to "Dues",
                    "message" to message
                )
            )
        }.build()

        Log.i("WorkManager", "Periodicity in millis: $periodicityInMillis")
        Log.i("WorkManager", "Delay in millis from now: ${delayInMillis - GRACE_PERIOD}")

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "${myWorkRequest.id}",
            ExistingPeriodicWorkPolicy.KEEP,
            myWorkRequest
        )
        // UUID usado en caso de que se quiera eliminar la notificación
        return myWorkRequest.id
    }

    fun deleteWork(uuid: String) {
        Log.i("WorkManager", "Deleted work: $uuid")
        WorkManager.getInstance(applicationContext).apply {
            cancelUniqueWork(uuid)
            pruneWork()
        }
    }

    override fun onBackPressed() {
        if (nav.currentDestination?.id == R.id.nav_login ||
            nav.currentDestination?.id == R.id.nav_home) moveTaskToBack(true)
        else super.onBackPressed()
    }
}