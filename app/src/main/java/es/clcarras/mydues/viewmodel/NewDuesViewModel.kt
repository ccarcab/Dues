package es.clcarras.mydues.viewmodel

import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.lifecycle.*
import es.clcarras.mydues.utils.Utility
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.model.MyDues
import es.clcarras.mydues.ui.DateDialogFragment
import kotlinx.coroutines.launch
import vadiole.colorpicker.ColorPickerDialog
import java.time.LocalDate
import java.util.*

class NewDuesViewModel(
    private val db: DuesRoomDatabase,
    cardColor: Int,
    contrastColor: Int
) : ViewModel() {

    class Factory(
        private val db: DuesRoomDatabase,
        private val cardColor: Int,
        private val contrastColor: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NewDuesViewModel(db, cardColor, contrastColor) as T
        }
    }

    private val _price = MutableLiveData("")
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

    private val _firstPayment = MutableLiveData("")
    val firstPayment: LiveData<String> get() = _firstPayment

    private val _cardColor = MutableLiveData(cardColor)
    val cardColor: LiveData<Int> get() = _cardColor

    private val _contrastColor = MutableLiveData(contrastColor)
    val contrastColor: LiveData<Int> get() = _contrastColor

    private val _error = MutableLiveData("")
    val error: LiveData<String> get() = _error

    private val _insert = MutableLiveData(false)
    val insert: LiveData<Boolean> get() = _insert

    private val _validInput = MutableLiveData(false)
    val validInput: LiveData<Boolean> get() = _validInput

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
            _firstPayment.value = Utility.formatLocalDate(LocalDate.of(year, month + 1, day))
        }
    }

    fun colorPicker(): ColorPickerDialog {
        return Utility.colorPicker(_cardColor.value)
            .onColorSelected { color: Int ->
                _cardColor.value = color
                val contrast = Utility.contrastColor(color)
                if (contrast != _contrastColor.value) {
                    _contrastColor.value = contrast
                }
            }
            .create()
    }

    fun checkData() {
        if (
            _price.value.isNullOrBlank() ||
            _name.value.isNullOrBlank() ||
            _firstPayment.value.isNullOrBlank()
        ) {
            _error.value = "Please check if you fill all the required fields."
            return
        }
        _validInput.value = true
    }

    fun saveDues(uuid: UUID) {
        viewModelScope.launch {
            db.duesDao().insert(
                MyDues(
                    price = _price.value!!,
                    name = _name.value!!,
                    description = _desc.value!!,
                    every = _every.value!!,
                    recurrence = _recurrence.value!!,
                    firstPayment = _firstPayment.value!!,
                    paymentMethod = _paymentMethod.value!!,
                    cardColor = _cardColor.value!!,
                    notification = uuid
                )
            )
            _insert.value = true
        }
    }

}