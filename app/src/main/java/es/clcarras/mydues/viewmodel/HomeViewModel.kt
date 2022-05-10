package es.clcarras.mydues.viewmodel

import androidx.lifecycle.*
import es.clcarras.mydues.adapter.DuesRowAdapter
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.model.MyDues
import es.clcarras.mydues.ui.DuesDetailsDialogFragment
import kotlinx.coroutines.launch

class HomeViewModel(
    private val database: DuesRoomDatabase
) : ViewModel() {

    class Factory(
        private val database: DuesRoomDatabase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(database) as T
        }
    }

    private val _dataLoaded = MutableLiveData(false)
    val dataLoaded: LiveData<Boolean> get() = _dataLoaded

    private val _deleted = MutableLiveData(false)
    val deleted: LiveData<Boolean> get() = _deleted

    private var dataList = mutableListOf<MyDues>()
    private var _adapter: DuesRowAdapter? = null
    val adapter get() = _adapter

    var detailsDialogFragment: DuesDetailsDialogFragment? = null

    fun deleteDues() {
        val i = dataList.indexOf(adapter?.selectedMyDues?.value)
        dataList.remove(adapter?.selectedMyDues?.value)
        _adapter?.notifyItemRemoved(i)
        _deleted.value = true
    }

    fun updateDues() {
        _adapter?.notifyItemChanged(
            dataList.indexOf(adapter?.selectedMyDues?.value)
        )
    }

    fun onDeleteComplete() {
        _deleted.value = false
    }

    fun loadDatabase() {
        viewModelScope.launch {
            with(database.duesDao()) {
                dataList = getAll()
                _adapter = DuesRowAdapter(dataList)
                _dataLoaded.value = true
            }
        }
    }
}