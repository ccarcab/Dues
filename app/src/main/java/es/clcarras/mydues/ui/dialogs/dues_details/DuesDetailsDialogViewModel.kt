package es.clcarras.mydues.ui.dialogs.dues_details

import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.clcarras.mydues.utils.Utility
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.database.Dues
import es.clcarras.mydues.ui.dialogs.DateDialogFragment
import es.clcarras.mydues.ui.home.HomeViewModel
import kotlinx.coroutines.launch
import vadiole.colorpicker.ColorPickerDialog
import java.time.LocalDate
import java.util.*

class DuesDetailsDialogViewModel(
    private val db: DuesRoomDatabase,
    private val dues: Dues,
    private val homeViewModel: HomeViewModel
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

    private val _error = MutableLiveData("")
    val error: LiveData<String> get() = _error

    private val _update = MutableLiveData(false)
    val update: LiveData<Boolean> get() = _update

    private val _delete = MutableLiveData(false)
    val delete: LiveData<Boolean> get() = _delete

    val spinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            val tv = (p1 as TextView?)
            tv?.setTextColor(Utility.contrastColor(_cardColor.value!!))
            _recurrence.value = tv?.text.toString()
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

    fun setNotification(uuid: UUID): UUID {
        val currentNotification = dues.notification
        dues.notification = uuid
        return if (!saveDues()) {
            dues.notification = currentNotification
            uuid
        } else
            currentNotification
    }

    fun datePicker(): DateDialogFragment {
        return DateDialogFragment.newInstance { _, year, month, day ->
            val selectedDate = Utility.formatLocalDate(LocalDate.of(year, month + 1, day))
            if (validInput()) {
                _firstPayment.value = selectedDate
            }
        }
    }

    fun colorPicker(): ColorPickerDialog {
        return Utility.colorPicker(_cardColor.value)
            .onColorSelected { color: Int ->
                _cardColor.value = color
            }.create()
    }

    fun deleteDues() {
        viewModelScope.launch {
            db.duesDao().remove(dues)
            _delete.value = true
            homeViewModel.deleteDues()
            close()
        }
    }

    private fun validInput(): Boolean {
        if (
            _price.value.isNullOrBlank() ||
            _name.value.isNullOrBlank() ||
            _firstPayment.value.isNullOrBlank() ||
            _every.value.isNullOrBlank()
        ) {
            _error.value = "Please check if you fill all the required fields."
            return false
        }
        return true
    }

    private fun saveDues(): Boolean {

        if (!validInput()) return false

        dues.price = _price.value!!
        dues.name = _name.value!!
        dues.description = _desc.value!!
        dues.every = _every.value!!
        dues.recurrence = _recurrence.value!!
        dues.firstPayment = _firstPayment.value!!
        dues.paymentMethod = _paymentMethod.value!!
        dues.cardColor = _cardColor.value!!

        viewModelScope.launch {
            db.duesDao().update(dues)
            homeViewModel.updateDues()
        }
        return true
    }

    fun onSave() {
        _update.value = saveDues()
    }

    fun close() {
        homeViewModel.detailsDialogFragment = null
        homeViewModel.adapter?.unSelectDues()
    }

}