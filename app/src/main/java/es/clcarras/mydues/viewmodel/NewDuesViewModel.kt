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

class NewDuesViewModel(
    private val args: NewDuesFragmentArgs,
    cardColor: Int,
    private val timeUnits: Array<String>
) : ViewModel() {

    class Factory(
        private val args: NewDuesFragmentArgs,
        private val cardColor: Int,
        private val timeUnits: Array<String>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NewDuesViewModel(args, cardColor, timeUnits) as T
    }

    private val _preloadDues = MutableLiveData<PreloadedDues>()
    val preloadDues: LiveData<PreloadedDues> get() = _preloadDues

    private val _price = MutableLiveData<String>()
    val price: LiveData<String> get() = _price

    private val _name = MutableLiveData("")
    val name: LiveData<String> get() = _name

    private val _desc = MutableLiveData("")
    val desc: LiveData<String> get() = _desc

    private val _every = MutableLiveData("1")
    val every: LiveData<String> get() = _every

    private val _paymentMethod = MutableLiveData("")
    val paymentMethod: LiveData<String> get() = _paymentMethod

    private val _recurrence = MutableLiveData("")
    val recurrence: LiveData<String> get() = _recurrence

    private val _firstPayment = MutableLiveData<Date>()
    val firstPayment: LiveData<Date> get() = _firstPayment

    private val _cardColor = MutableLiveData(cardColor)
    val cardColor: LiveData<Int> get() = _cardColor

    private val _error = MutableLiveData("")
    val error: LiveData<String> get() = _error

    private val _insert = MutableLiveData(false)
    val insert: LiveData<Boolean> get() = _insert

    private val _validInput = MutableLiveData(false)
    val validInput: LiveData<Boolean> get() = _validInput

    private var nextPayment: Calendar? = null

    val spinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            _recurrence.value = (p1 as TextView?)?.text.toString()
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {}
    }

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

    fun datePicker(): DateDialogFragment {
        return DateDialogFragment.newInstance { _, year, month, day ->
            _firstPayment.value = Utility.getDate(year, month + 1, day)
        }
    }

    fun colorPicker(): ColorPickerDialog {
        return Utility.colorPicker(_cardColor.value)
            .onColorSelected { color: Int ->
                _cardColor.value = color
            }
            .create()
    }

    fun checkData() {
        if (
            _price.value.isNullOrBlank() ||
            _name.value.isNullOrBlank() ||
            _firstPayment.value == null
        ) {
            _error.value = "Please check if you fill all the required fields."
            return
        }
        _validInput.value = true
    }

    fun saveDues(uuid: String, msg: String) {
        viewModelScope.launch {

            val myDues = MyDues(
                price = _price.value!!.toInt(),
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

            MyDuesDao().newDues(myDues).addOnSuccessListener { duesDoc ->
                myDues.id = duesDoc.id
                MyDuesDao().updateDues(myDues).addOnSuccessListener {
                    _insert.value = true
                }
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
            }
        }
    }

    fun millisUntilNextPayment(): Long {
        nextPayment = Calendar.getInstance()
        // Se establece la fecha de primer pago
        nextPayment!!.time = firstPayment.value!!
        // Se añade el tiempo hasta el próximo pago
        nextPayment!!.add(Calendar.HOUR_OF_DAY, periodicityInHours())
        // Se calcula el tiempo que queda desde ahora hasta el próximo pago
        return abs(nextPayment!!.time.time - System.currentTimeMillis())
    }

    fun periodicityInHours() = when (recurrence.value) {
        timeUnits[0] -> 1
        timeUnits[1] -> 7
        timeUnits[2] -> 30
        timeUnits[3] -> 365
        else -> 0
    } * (every.value?.toInt() ?: 1) * 24


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