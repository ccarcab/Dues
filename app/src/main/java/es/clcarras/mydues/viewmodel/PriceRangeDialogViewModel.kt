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

class PriceRangeDialogViewModel(
    private val timeUnits: Array<String>
) : ViewModel() {

    companion object {
        const val INIT_DATE = 0
        const val END_DATE = 1
    }

    class Factory(
        private val timeUnits: Array<String>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PriceRangeDialogViewModel(timeUnits) as T
    }

    private val _initDate = MutableLiveData<Date>()
    val initDate: LiveData<Date> get() = _initDate

    private val _endDate = MutableLiveData<Date>()
    val endDate: LiveData<Date> get() = _endDate

    private val _totalPrice = MutableLiveData(0.0)
    val totalPrice: LiveData<Double> get() = _totalPrice

    init {
        _initDate.value = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC))
    }

    fun datePicker(dateId: Int): DateDialogFragment {
        val currentDate = if (dateId == INIT_DATE) _initDate.value else _endDate.value

        return DateDialogFragment.newInstance(currentDate) { _, year, month, day ->
            val cal = Calendar.getInstance()
            cal.set(year, month, day)
            if (cal.time.after(Date.from(Instant.now())) || cal.time.equals(Date.from(Instant.now())))
                if (dateId == INIT_DATE)
                    _initDate.value = cal.time
                else if (cal.time.after(Date.from(Instant.now())) && !cal.time.equals(_initDate.value))
                    _endDate.value = cal.time

            checkPrice()
        }
    }

    private fun checkPrice() {
        if (_initDate.value != null && _endDate.value != null) {
            _totalPrice.value = 0.0

            if (_initDate.value!!.after(_endDate.value) || _initDate.value!! == _endDate.value)
                return

            val daysBetween =
                ((_endDate.value!!.time - _initDate.value!!.time) /
                        (1000 * 60 * 60 * 24))
            Log.i("PriceRangeDialogViewModel", "$daysBetween")
            MyDuesDao().getMyDues().addOnSuccessListener { col ->
                for (doc in col) {
                    _totalPrice.value = _totalPrice.value!!.plus(
                        (Utility.calculateDailyPrice(
                            doc.toObject(MyDues::class.java),
                            timeUnits
                        ) * daysBetween)
                    )
                }
            }
        }
    }
}