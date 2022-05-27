package es.clcarras.mydues.viewmodel

import android.icu.util.Calendar
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.lifecycle.*
import es.clcarras.mydues.database.MyDuesDao
import es.clcarras.mydues.database.PreloadDuesDao
import es.clcarras.mydues.database.WorkerDao
import es.clcarras.mydues.model.MyDues
import es.clcarras.mydues.model.PreloadedDues
import es.clcarras.mydues.model.Worker
import es.clcarras.mydues.ui.DateDialogFragment
import es.clcarras.mydues.utils.Utility
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

/**
 * ViewModel del Fragment de detalles de cuota
 */
class DuesDetailsDialogViewModel(
    private val _myDues: MyDues, // Cuota seleccionada en la vista Home
    private val homeViewModel: HomeViewModel, // ViewModel de la vista Home para comunicarse
    private val recurrences: Array<String> // Recurrencias existentes
) : ViewModel() {

    /**
     * Clase Factory del ViewModel, usado para pasar parámetros al mismo
     */
    class Factory(
        private val myDues: MyDues?,
        private val homeViewModel: HomeViewModel?,
        private val recurrences: Array<String>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DuesDetailsDialogViewModel(myDues!!, homeViewModel!!, recurrences) as T
    }

    // LiveData para almacenar los datos de la cuota precargada
    private val _preloadDues = MutableLiveData<PreloadedDues>()
    val preloadDues: LiveData<PreloadedDues> get() = _preloadDues

    // LiveData para almacenar el precio de la cuota
    private val _price = MutableLiveData(_myDues.price.toString())
    val price: LiveData<String> get() = _price

    // LiveData para almacenar el nombre de la cuota
    private val _name = MutableLiveData(_myDues.name!!)
    val name: LiveData<String> get() = _name

    // LiveData para almacenar la descripción de la cuota
    private val _desc = MutableLiveData(_myDues.description!!)
    val desc: LiveData<String> get() = _desc

    // LiveData para almacenar cada cuánto se paga la cuota
    private val _every = MutableLiveData(_myDues.every.toString())
    val every: LiveData<String> get() = _every

    // LiveData para almacenar el método de pago de la cuota
    private val _paymentMethod = MutableLiveData(_myDues.paymentMethod!!)
    val paymentMethod: LiveData<String> get() = _paymentMethod

    // LiveData para almacenar la recurrencia de la cuota
    private val _recurrence = MutableLiveData(_myDues.recurrence!!)
    val recurrence: LiveData<String> get() = _recurrence

    // LiveData para almacenar la fecha de primer pago de la cuota
    private val _firstPayment = MutableLiveData(_myDues.firstPayment!!)
    val firstPayment: LiveData<Date> get() = _firstPayment

    // LiveData para almacenar el color de la tarjeta de la cuota
    private val _cardColor = MutableLiveData(_myDues.cardColor)
    val cardColor: LiveData<Int> get() = _cardColor

    // LiveData para almacenar el mensaje de error en caso de que lo haya
    private val _error = MutableLiveData("")
    val error: LiveData<String> get() = _error

    // LiveData para notificar que se ha completado la actualización de datos de la cuota
    private val _update = MutableLiveData(false)
    val update: LiveData<Boolean> get() = _update

    // LiveData para notificar que se ha completado la eliminación de la cuota
    private val _delete = MutableLiveData(false)
    val delete: LiveData<Boolean> get() = _delete

    // Métodos para obtener datos de la cuota
    val `package` get() = _myDues.`package`
    val notificationUUID get() = _myDues.notificationUUID!!

    // Variable para almacenar la fecha del siguiente pago
    private var nextPayment: Calendar? = null

    // Objeto que almacena el evento de escucha del elemento Spinner
    val spinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            _recurrence.value = (p1 as TextView?)?.text.toString()
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {}
    }

    // Métodos Setter para los live data //
    fun setPrice(text: String) {
        _price.value = text
    }

    fun setName(text: String) {
        _name.value = text
    }

    fun setDesc(text: String) {
        _desc.value = text
    }

    fun setEvery(text: String) {
        _every.value = text
    }

    fun setPaymentMethod(text: String) {
        _paymentMethod.value = text
    }

    /**
     * Método para cambiar la notificación asociada a la cuota
     */
    fun setNotification(uuid: String, msg: String): String {
        val currentNotification = _myDues.notificationUUID
        _myDues.notificationUUID = uuid
        // Si no se guardan los cambios se vuelve a establecer el UUID anterior
        return if (!saveDues()) {
            _myDues.notificationUUID = currentNotification
            uuid
        } else {
            // Si guardan los cambios se actualiza los datos del worker asociado a la cuota
            val worker = Worker(
                uuid = uuid,
                targetDate = nextPayment!!.time,
                periodicity = periodicityInHours(),
                message = msg
            )
            WorkerDao().newWorker(worker).addOnSuccessListener { workDoc ->
                worker.id = workDoc.id
                WorkerDao().updateWorker(worker)
            }
            currentNotification.toString()
        }
    }

    /**
     * Método que devuelve un cuadro de diálogo para que el usuario elija una fecha
     */
    fun datePicker(): DateDialogFragment {
        return DateDialogFragment.newInstance(_firstPayment.value) { _, year, month, day ->
            if (validInput()) {
                val cal = Calendar.getInstance()
                cal.set(year, month, day)
                _firstPayment.value = cal.time
            }
        }
    }

    /**
     * Método que devuelve un cuadro de diálogo para que el usuario elija un color
     */
    fun colorPicker() = Utility.colorPicker(_cardColor.value)
        .onColorSelected { color: Int ->
            _cardColor.value = color
        }.create()

    /**
     * Método usado para eliminar la cuota
     */
    fun deleteDues() {
        viewModelScope.launch {
            _delete.value = true
            MyDuesDao().deleteDues(_myDues)
            WorkerDao().deleteWorkerByUUID(_myDues.notificationUUID!!)
            homeViewModel.deleteDues()
            close()
        }
    }

    /**
     * Método que comprueba que se han introducido los datos requeridos
     */
    private fun validInput() = if (
        _price.value.isNullOrBlank() ||
        _name.value.isNullOrBlank() ||
        _firstPayment.value == null ||
        _every.value.isNullOrBlank()
    ) {
        //
        _error.value = "Please check if you fill all the required fields."
        false
    } else true

    /**
     * Método que intenta guardar los cambios realizados en la cuota
     */
    private fun saveDues(): Boolean {

        // Si hay error en los datos introducidos
        if (!validInput()) return false

        // Se actualizan los datos de la cuota
        _myDues.price = _price.value!!.toDouble()
        _myDues.name = _name.value!!
        _myDues.description = _desc.value!!
        _myDues.every = _every.value!!.toInt()
        _myDues.recurrence = _recurrence.value!!
        _myDues.firstPayment = _firstPayment.value!!
        _myDues.paymentMethod = _paymentMethod.value!!
        _myDues.cardColor = _cardColor.value!!

        // Se lanza un hilo para actualizar la cuota
        viewModelScope.launch {
            MyDuesDao().updateDues(_myDues)
            homeViewModel.updateDues()
        }
        return true
    }

    /**
     * Método lamado para guardar los datos de la cuota
     */
    fun onSave() {
        _update.value = saveDues()
    }

    /**
     * Método llamado para cerrar el cuadro de diálogo
     */
    fun close() {
        homeViewModel.detailsDialogFragment = null
        homeViewModel.adapter?.unSelectDues()
    }

    /**
     * Método para calcular el tiempo que queda en milisegundos hasta el próximo pago
     */
    fun millisUntilNextPayment(): Long {
        nextPayment = Calendar.getInstance()
        // Se establece la fecha de primer pago
        nextPayment!!.time = firstPayment.value!!

        // Se añade el tiempo hasta el próximo pago
        nextPayment!!.add(Calendar.HOUR_OF_DAY, periodicityInHours())
        // Se calcula el tiempo que queda desde ahora hasta el próximo pago
        return abs(nextPayment!!.time.time - System.currentTimeMillis())
    }

    /**
     * Método usado para calcular la periodicidad de la cuota en horas
     */
    fun periodicityInHours() = when (recurrence.value) {
        recurrences[0] -> 1
        recurrences[1] -> 7
        recurrences[2] -> 30
        recurrences[3] -> 365
        else -> 0
    } * (every.value?.toInt() ?: 1) * 24

    /**
     * Método para comprobar si la cuota seleccionada es una cuota precargada
     */
    fun checkSelectedDues() {
        if (!_myDues.`package`.isNullOrBlank()) {
            viewModelScope.launch {
                PreloadDuesDao().getPreloadDueByPackage(_myDues.`package`!!)
                    .addOnSuccessListener { docs ->
                        _preloadDues.value = docs.single().toObject(PreloadedDues::class.java)
                    }
            }
        }
    }

}