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
import es.clcarras.mydues.ui.NewDuesFragmentArgs
import es.clcarras.mydues.utils.Utility
import kotlinx.coroutines.launch
import vadiole.colorpicker.ColorPickerDialog
import java.util.*
import kotlin.math.abs

/**
 * ViewModel del Fragment de nueva cuota
 */
class NewDuesViewModel(
    private val args: NewDuesFragmentArgs, // Argumentos recibidos por el fragment
    cardColor: Int, // Color inicial de la tarjeta
    private val recurrences: Array<String> // Array de recurencias
) : ViewModel() {

    /**
     * Clase Factory del ViewModel, usado para pasar parámetros al mismo
     */
    class Factory(
        private val args: NewDuesFragmentArgs,
        private val cardColor: Int,
        private val timeUnits: Array<String>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NewDuesViewModel(args, cardColor, timeUnits) as T
    }

    // LiveData para almacenar los datos de la cuota precargada
    private val _preloadDues = MutableLiveData<PreloadedDues>()
    val preloadDues: LiveData<PreloadedDues> get() = _preloadDues

    // LiveData para almacenar el precio de la cuota
    private val _price = MutableLiveData<String>()
    val price: LiveData<String> get() = _price

    // LiveData para almacenar el nombre de la cuota
    private val _name = MutableLiveData("")
    val name: LiveData<String> get() = _name

    // LiveData para almacenar la descripción de la cuota
    private val _desc = MutableLiveData("")
    val desc: LiveData<String> get() = _desc

    // LiveData para almacenar cada cuánto se paga la cuota
    private val _every = MutableLiveData("1")
    val every: LiveData<String> get() = _every

    // LiveData para almacenar el método de pago de la cuota
    private val _paymentMethod = MutableLiveData("")
    val paymentMethod: LiveData<String> get() = _paymentMethod

    // LiveData para almacenar la recurrencia de la cuota
    private val _recurrence = MutableLiveData("")
    val recurrence: LiveData<String> get() = _recurrence

    // LiveData para almacenar la fecha de primer pago de la cuota
    private val _firstPayment = MutableLiveData<Date>()
    val firstPayment: LiveData<Date> get() = _firstPayment

    // LiveData para almacenar el color de la tarjeta de la cuota
    private val _cardColor = MutableLiveData(cardColor)
    val cardColor: LiveData<Int> get() = _cardColor

    // LiveData para almacenar el mensaje de error en caso de que lo haya
    private val _error = MutableLiveData("")
    val error: LiveData<String> get() = _error

    // LiveData para notificar que se ha completado la inserción de la nueva cuota
    private val _insert = MutableLiveData(false)
    val insert: LiveData<Boolean> get() = _insert

    // LiveData para notificar que se han proporcionado datos válidos
    private val _validInput = MutableLiveData(false)
    val validInput: LiveData<Boolean> get() = _validInput

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
     * Método que devuelve un cuadro de diálogo para que el usuario elija una fecha
     */
    fun datePicker(): DateDialogFragment {
        return DateDialogFragment.newInstance(_firstPayment.value) { _, year, month, day ->
            val cal = Calendar.getInstance()
            cal.set(year, month, day)
            _firstPayment.value = cal.time
        }
    }

    /**
     * Método que devuelve un cuadro de diálogo para que el usuario elija un color
     */
    fun colorPicker(): ColorPickerDialog {
        return Utility.colorPicker(_cardColor.value)
            .onColorSelected { color: Int ->
                _cardColor.value = color
            }
            .create()
    }

    /**
     * Método que comprueba que se han introducido los datos requeridos
     */
    fun checkData() {
        if (
            _price.value.isNullOrBlank() ||
            _name.value.isNullOrBlank() ||
            _every.value.isNullOrBlank() ||
            _firstPayment.value == null
        ) {
            _error.value = "Please check if you fill all the required fields."
            return
        }
        _validInput.value = true
    }

    /**
     * Método que intenta guardar la nueva cuota
     */
    fun saveDues(uuid: String, msg: String) {
        viewModelScope.launch {

            // Se crea un data class con los datos de la nueva cuota
            val myDues = MyDues(
                price = _price.value!!.toDouble(),
                name = _name.value,
                description = _desc.value,
                every = _every.value!!.toInt(),
                recurrence = _recurrence.value,
                firstPayment = _firstPayment.value,
                paymentMethod = _paymentMethod.value,
                cardColor = _cardColor.value!!,
                notificationUUID = uuid,
                `package` = _preloadDues.value?.`package`
            )

            // Se añade la cuota a Firebase
            MyDuesDao().newDues(myDues).addOnSuccessListener { duesDoc ->
                // Se almacena el id del documento como un dato de la cuota
                myDues.id = duesDoc.id
                // Se actualiza la cuota recién creada
                MyDuesDao().updateDues(myDues).addOnSuccessListener {
                    // Se crea un data class para alamacenar los datos del worker
                    val worker = Worker(
                        uuid = uuid,
                        targetDate = nextPayment!!.time,
                        periodicity = periodicityInHours(),
                        message = msg
                    )
                    // Se añade el worker a Firebase
                    WorkerDao().newWorker(worker).addOnSuccessListener { workDoc ->
                        // Se obtiene el id del documento autogenerado
                        worker.id = workDoc.id
                        // Se actualiza el worker
                        WorkerDao().updateWorker(worker).addOnSuccessListener {
                            // Se notifica que se ha completado la inserción
                            _insert.value = true
                        }
                    }
                }
            }
        }
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
        if (args.pkg.isNotBlank()) {
            viewModelScope.launch {
                PreloadDuesDao().getPreloadDueByPackage(args.pkg).addOnSuccessListener { docs ->
                    _preloadDues.value = docs.single().toObject(PreloadedDues::class.java)
                    _cardColor.value = _preloadDues.value?.color
                }
            }
        }
    }

}