package es.clcarras.mydues.viewmodel

import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.lifecycle.*
import es.clcarras.mydues.database.MyDuesDao
import es.clcarras.mydues.database.PreloadDuesDao
import es.clcarras.mydues.model.MyDues
import es.clcarras.mydues.model.PreloadedDues
import es.clcarras.mydues.ui.DateDialogFragment
import es.clcarras.mydues.utils.Utility
import kotlinx.coroutines.launch
import vadiole.colorpicker.ColorPickerDialog
import java.util.*

class DuesDetailsDialogViewModel(
    private val _myDues: MyDues,
    private val homeViewModel: HomeViewModel
) : ViewModel() {

    class Factory(
        private val myDues: MyDues?,
        private val homeViewModel: HomeViewModel?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DuesDetailsDialogViewModel(myDues!!, homeViewModel!!) as T
    }

    private val _preloadDues = MutableLiveData<PreloadedDues>()
    val preloadDues: LiveData<PreloadedDues> get() = _preloadDues

    private val _price = MutableLiveData(_myDues.price.toString())
    val price: LiveData<String> get() = _price

    private val _name = MutableLiveData(_myDues.name!!)
    val name: LiveData<String> get() = _name

    private val _desc = MutableLiveData(_myDues.description!!)
    val desc: LiveData<String> get() = _desc

    private val _every = MutableLiveData(_myDues.every.toString())
    val every: LiveData<String> get() = _every

    private val _paymentMethod = MutableLiveData(_myDues.paymentMethod!!)
    val paymentMethod: LiveData<String> get() = _paymentMethod

    private val _recurrence = MutableLiveData(_myDues.recurrence!!)
    val recurrence: LiveData<String> get() = _recurrence

    private val _firstPayment = MutableLiveData(_myDues.firstPayment!!)
    val firstPayment: LiveData<Date> get() = _firstPayment

    private val _cardColor = MutableLiveData(_myDues.cardColor)
    val cardColor: LiveData<Int> get() = _cardColor

    private val _error = MutableLiveData("")
    val error: LiveData<String> get() = _error

    private val _update = MutableLiveData(false)
    val update: LiveData<Boolean> get() = _update

    private val _delete = MutableLiveData(false)
    val delete: LiveData<Boolean> get() = _delete

    val `package` get() = _myDues.`package`

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

    fun setNotification(uuid: String): String {
        val currentNotification = _myDues.notification
        _myDues.notification = uuid
        return if (!saveDues()) {
            _myDues.notification = currentNotification
            uuid
        } else
            currentNotification.toString()
    }

    fun datePicker(): DateDialogFragment {
        return DateDialogFragment.newInstance { _, year, month, day ->
            val selectedDate = Utility.getDate(year, month + 1, day)
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
            MyDuesDao().deleteDoc(_myDues)
            _delete.value = true
            homeViewModel.deleteDues()
            close()
        }
    }

    private fun validInput(): Boolean {
        if (
            _price.value.isNullOrBlank() ||
            _name.value.isNullOrBlank() ||
            _firstPayment.value == null ||
            _every.value.isNullOrBlank()
        ) {
            _error.value = "Please check if you fill all the required fields."
            return false
        }
        return true
    }

    private fun saveDues(): Boolean {

        if (!validInput()) return false

        _myDues.price = _price.value!!.toInt()
        _myDues.name = _name.value!!
        _myDues.description = _desc.value!!
        _myDues.every = _every.value!!.toInt()
        _myDues.recurrence = _recurrence.value!!
        _myDues.firstPayment = _firstPayment.value!!
        _myDues.paymentMethod = _paymentMethod.value!!
        _myDues.cardColor = _cardColor.value!!

        viewModelScope.launch {
            MyDuesDao().updateDoc(_myDues)
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