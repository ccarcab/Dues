package es.clcarras.mydues.ui.dialogs.dues_details

import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.clcarras.mydues.Utility
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.model.Dues
import es.clcarras.mydues.ui.dialogs.DateDialogFragment
import kotlinx.coroutines.launch
import vadiole.colorpicker.ColorPickerDialog

class DuesDetailsDialogViewModel(
    private val db: DuesRoomDatabase,
    private val dues: Dues
) : ViewModel() {

    private val _price = MutableLiveData(dues.price)
    val price: LiveData<String> get() = _price

    private val _name = MutableLiveData(dues.name)
    val name: LiveData<String> get() = _name

    private val _desc = MutableLiveData(dues.description)
    val desc: LiveData<String> get() = _desc

    private val _every = MutableLiveData(dues.every)
    val every: LiveData<String> get() = _every

    private val _paymentMethod = MutableLiveData(dues.paymentMethod)
    val paymentMethod: LiveData<String> get() = _paymentMethod

    private val _recurrence = MutableLiveData(dues.recurrence)
    val recurrence: LiveData<String> get() = _recurrence

    private val _firstPayment = MutableLiveData(dues.firstPayment)
    val firstPayment: LiveData<String> get() = _firstPayment

    private val _cardColor = MutableLiveData(dues.cardColor)
    val cardColor: LiveData<Int> get() = _cardColor

    private val _contrastColor = MutableLiveData(Utility.contrastColor(dues.cardColor))
    val contrastColor: LiveData<Int> get() = _contrastColor

    private val _error = MutableLiveData("")
    val error: LiveData<String> get() = _error

    private val _update = MutableLiveData(false)
    val update: LiveData<Boolean> get() = _update

    private val _delete = MutableLiveData(false)
    val delete: LiveData<Boolean> get() = _delete

    val spinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            val text = (p1 as TextView?)?.text.toString()
            _recurrence.value = text
            dues.recurrence = text
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {}

    }

    fun setPrice(text: String) {
        _price.value = text
        dues.price = text
    }

    fun setName(text: String) {
        _name.value = text
        dues.name = text
    }

    fun setDesc(text: String) {
        _desc.value = text
        dues.description = text
    }

    fun setEvery(text: String) {
        _every.value = text
        dues.every = text
    }

    fun setPaymentMethod(text: String) {
        _paymentMethod.value = text
        dues.paymentMethod = text
    }

    fun datePicker(): DateDialogFragment {
        return DateDialogFragment.newInstance { _, year, month, day ->
            // +1 because January is zero
            val selectedDate = "$day / ${month + 1} / $year"
            _firstPayment.value = selectedDate
            dues.firstPayment = selectedDate
        }
    }

    fun colorPicker(): ColorPickerDialog {
        return Utility.colorPicker(_cardColor.value)
            .onColorSelected { color: Int ->
                _cardColor.value = color
                dues.cardColor = color
                val contrast = Utility.contrastColor(color)
                if (contrast != _contrastColor.value) {
                    _contrastColor.value = contrast
                }
            }
            .create()
    }

    fun deleteDues() {
        viewModelScope.launch {
            db.duesDao().remove(dues)
            _delete.value = true
        }
    }

    fun saveDues() {
        if (
            _price.value.isNullOrBlank() ||
            _name.value.isNullOrBlank() ||
            _firstPayment.value.isNullOrBlank()
        ) {
            _error.value = "Please check if you fill all the required fields."
            return
        }

        viewModelScope.launch {
            db.duesDao().update(dues)
            _update.value = true
        }
    }

}