package es.clcarras.mydues

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.work.*
import es.clcarras.mydues.constants.GRACE_PERIOD
import es.clcarras.mydues.database.WorkerDao
import es.clcarras.mydues.databinding.ActivityMainBinding
import es.clcarras.mydues.service.DuesNotificationWorker
import es.clcarras.mydues.utils.Utility
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Clase MainActivity que será la encargada de inicializar la aplicación
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var nav: NavController

    /**
     * Método que inicializa la aplicación
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Dues) // Se pone el tema por defecto
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)
            setSupportActionBar(bottomAppBar)
        }
        nav = findNavController(R.id.nav_host_fragment)
    }

    /**
     * Método para obtener la barra inferior
     */
    fun getBottomAppBar() = binding.bottomAppBar

    /**
     * Método para obtener el botón FAB
     */
    fun getFab() = binding.fab

    /**
     * Método para crear un worker periódico para mostrar notificaciones y obtener su UUID
     */
    fun createWorkRequest(
        message: String,
        periodicityInMillis: Long,
        delayInMillis: Long,
        uuid: String? = null
    ): UUID {

        val myWorkRequest = PeriodicWorkRequestBuilder<DuesNotificationWorker>(
            periodicityInMillis, TimeUnit.MILLISECONDS
        ).apply {
            if (delayInMillis - GRACE_PERIOD > 0)
                setInitialDelay(delayInMillis - GRACE_PERIOD, TimeUnit.MILLISECONDS)

            // Camportamiento en segundo plano del worker
            setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )

            // Parámetros del worker
            setInputData(
                workDataOf(
                    "title" to "Dues",
                    "message" to message
                )
            )
        }.build()

        // Si el UUID recibido es nulo
        val uniqueName = uuid ?: myWorkRequest.id.toString()

        // Se encola el worker y se le asigna un nombre único, que será su UUID
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.KEEP,
            myWorkRequest
        )
        // UUID usado en caso de que se quiera eliminar la notificación
        return myWorkRequest.id
    }

    /**
     * Método que elimina un worker a través de su UUID
     */
    fun deleteWork(uuid: String) {
        WorkManager.getInstance(applicationContext).apply {
            cancelUniqueWork(uuid)
            pruneWork()
        }
        // Se borra de la base de datos
        WorkerDao().deleteWorkerByUUID(uuid)
    }

    /**
     * Método ejecutado al pulsar el botón de back
     */
    override fun onBackPressed() {
        // Si el usuario se encuentra en la vista Login o Home se minimiza la aplicación
        if (nav.currentDestination?.id == R.id.nav_login ||
            nav.currentDestination?.id == R.id.nav_home
        ) moveTaskToBack(true)

        // En caso contrario se ejecuta la acción por defecto
        else super.onBackPressed()
    }

    /**
     * Método que cambia el color de la barra de estado del sistema en función del color recibido
     */
    fun changeStatusBarColor(it: Int) {
        window.statusBarColor = it
        val isLight = Utility.contrastColor(it) == Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            window.insetsController?.setSystemBarsAppearance(
                if (isLight) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        else window.decorView.systemUiVisibility =
            if (isLight)
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            else
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

}
