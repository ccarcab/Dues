package es.clcarras.mydues.viewmodel

import androidx.lifecycle.*
import es.clcarras.mydues.adapter.DuesHomeAdapter
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.model.MyDues
import es.clcarras.mydues.ui.DuesDetailsDialogFragment
import kotlinx.coroutines.launch

class HomeViewModel(
    private val database: DuesRoomDatabase,
    private val recurrences: Array<String>
) : ViewModel() {

    class Factory(
        private val database: DuesRoomDatabase,
        private val recurrences: Array<String>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            HomeViewModel(database, recurrences) as T
    }

    private val _dataLoaded = MutableLiveData(false)
    val dataLoaded: LiveData<Boolean> get() = _dataLoaded

    private val _deleted = MutableLiveData(false)
    val deleted: LiveData<Boolean> get() = _deleted

    private val _totalPrice = MutableLiveData(0)
    val totalPrice: LiveData<Int> get() = _totalPrice

    private var dataList = mutableListOf<MyDues>()
    private var _adapter: DuesHomeAdapter? = null
    val adapter get() = _adapter

    var detailsDialogFragment: DuesDetailsDialogFragment? = null

    fun deleteDues() {
        val deletedDues = adapter!!.selectedMyDues.value!!
        _totalPrice.value = _totalPrice.value?.minus(deletedDues.price.toInt())

        val i = dataList.indexOf(deletedDues)
        dataList.remove(deletedDues)
        _adapter?.notifyItemRemoved(i)

        _deleted.value = true
    }

    fun updateDues() {
        val updatedDues = adapter!!.selectedMyDues.value!!
        _adapter?.notifyItemChanged(dataList.indexOf(updatedDues))
        reloadTotalPrice()
    }

    private fun reloadTotalPrice() {
        _totalPrice.value = 0
        dataList.forEach {
            _totalPrice.value = _totalPrice.value?.plus(getPriceByRecurrence(it))
        }
    }

    fun onDeleteComplete() {
        _deleted.value = false
    }

    fun loadDatabase() {
        viewModelScope.launch {
            with(database.duesDao()) {
                dataList = getAll()
                dataList.forEach {
                    _totalPrice.value = _totalPrice.value?.plus(getPriceByRecurrence(it))
                }
                _adapter = DuesHomeAdapter(dataList)
                _dataLoaded.value = true
            }
        }
    }

    private fun getPriceByRecurrence(myDues: MyDues) =
        when (myDues.recurrence) {
            recurrences[0] -> (myDues.price.toDouble() * 30)
            recurrences[1] -> myDues.price.toDouble() * 4.3
            recurrences[3] -> myDues.price.toDouble() / 12
            else -> myDues.price.toInt()
        }.toInt() / myDues.every.toInt()

}