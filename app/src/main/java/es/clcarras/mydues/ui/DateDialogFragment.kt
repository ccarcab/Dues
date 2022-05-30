package es.clcarras.mydues.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

/**
 * Fragmento que muestra un cuadro de diálogo para seleccionar una fecha
 */
class DateDialogFragment(
    private val initialDate: Date? // Fecha mostrada inicialmente
) : DialogFragment() {

    // Constructor sin parámetros
    constructor() : this(null)

    // Objeto listener para detectar cuando hay un cambio de fecha
    private var listener: DatePickerDialog.OnDateSetListener? = null

    /**
     * Método que crea el cuadro de diálogo
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Calendario para obtener la fecha
        val c = Calendar.getInstance()

        // Si se ha recibido una fecha inicial se establece como fecha por defecto
        if (initialDate != null) c.time = initialDate

        // Se obtiene el año, mes y día del calendario
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Se crea una instancia del cuadro de diálogo y se devuelve
        return DatePickerDialog(requireActivity(), listener, year, month, day)
    }

    companion object {

        // Tag que identificará al curadro de diálogo
        const val TAG = "DateDialogFragment"

        // Método usado para crear nuevas instancias del cuadro de diálogo
        fun newInstance(
            initialDate: Date?, // Fecha inicial
            listener: DatePickerDialog.OnDateSetListener // Evento de escucha al cambio de fecha
        ): DateDialogFragment = DateDialogFragment(initialDate).apply {
            this.listener = listener
        }
    }

}