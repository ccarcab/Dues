package es.clcarras.mydues.viewmodel

import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.clcarras.mydues.database.MyDuesDao
import es.clcarras.mydues.model.MyDues
import es.clcarras.mydues.ui.DateDialogFragment
import es.clcarras.mydues.utils.Utility
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * ViewModel del Fragment de la vista de cálculo de precio
 */
class PriceRangeDialogViewModel(
    private val recurrences: Array<String> // Listado de recurrencias
) : ViewModel() {

    companion object {
        // Constantes para identificar en que campo se quiere almacenar la fecha seleccionada
        const val INIT_DATE = 0
        const val END_DATE = 1
    }

    /**
     * Clase Factory del ViewModel, usado para pasar parámetros al mismo
     */
    class Factory(
        private val timeUnits: Array<String>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PriceRangeDialogViewModel(timeUnits) as T
    }

    // LiveData para almacenar la fecha de inicio
    private val _initDate = MutableLiveData<Date>()
    val initDate: LiveData<Date> get() = _initDate

    // LiveData para almacenar la fecha de fin
    private val _endDate = MutableLiveData<Date>()
    val endDate: LiveData<Date> get() = _endDate

    // LiveData para almacenar el precio total
    private val _totalPrice = MutableLiveData(0.0)
    val totalPrice: LiveData<Double> get() = _totalPrice

    init {
        // La fecha de inicio por defecto será la fecha actual
        _initDate.value = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC))
    }

    /**
     * Método que muestra un date picker para seleccionar una fecha
     */
    fun datePicker(dateId: Int): DateDialogFragment {
        val currentDate = if (dateId == INIT_DATE) _initDate.value else _endDate.value

        return DateDialogFragment.newInstance(currentDate) { _, year, month, day ->
            val cal = Calendar.getInstance()
            val today = Date.from(Instant.now())
            cal.set(year, month, day)
            // Se añade la fecha si es igual o posterior a la fecha actual
            if (cal.time.after(today) || cal.time.equals(today))
                if (dateId == INIT_DATE)
                    _initDate.value = cal.time
                else if (cal.time.after(today) && !cal.time.equals(_initDate.value))
                    _endDate.value = cal.time

            // Se calcula el precio total
            checkPrice()
        }
    }

    /**
     * Método que calcula el precio total entre dos fechas
     */
    private fun checkPrice() {
        if (_initDate.value != null && _endDate.value != null) {
            _totalPrice.value = 0.0

            if (_initDate.value!!.after(_endDate.value) || _initDate.value!! == _endDate.value)
                return

            // Días que hay entre las dos fechas
            val daysBetween =
                ((_endDate.value!!.time - _initDate.value!!.time) /
                        (1000 * 60 * 60 * 24))

            // Se obtienen todas las cuotas y se calcula su precio diario
            MyDuesDao().getMyDues().addOnSuccessListener { col ->
                for (doc in col) {
                    _totalPrice.value = _totalPrice.value!!.plus(
                        (Utility.calculateDailyPrice(
                            doc.toObject(MyDues::class.java),
                            recurrences
                        ) * daysBetween)
                    )
                }
            }
        }
    }
}