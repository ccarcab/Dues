package es.clcarras.mydues.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.model.Dues
import es.clcarras.mydues.ui.dialogs.dues_details.DuesDetailsDialogFragment
import kotlinx.coroutines.launch

class HomeViewModel(
    private val database: DuesRoomDatabase
) : ViewModel() {

    private val _dataLoaded = MutableLiveData(false)
    val dataLoaded: LiveData<Boolean> get() = _dataLoaded

    private val _deleted = MutableLiveData(false)
    val deleted: LiveData<Boolean> get() = _deleted

    private var dataList = mutableListOf<Dues>()
    private var _adapter: DuesAdapter? = null
    val adapter get() = _adapter

    var detailsDialogFragment: DuesDetailsDialogFragment? = null

    fun deleteDues() {
        val i = dataList.indexOf(adapter?.selectedDues?.value)
        dataList.remove(adapter?.selectedDues?.value)
        _adapter?.notifyItemRemoved(i)
        _deleted.value = true
    }

    fun updateDues() {
        _adapter?.notifyItemChanged(
            dataList.indexOf(adapter?.selectedDues?.value)
        )
    }

    fun onDeleteComplete() {
        _deleted.value = false
    }

    fun loadDatabase() {
        viewModelScope.launch {
            with(database.duesDao()) {
                dataList = getAll()
                _adapter = DuesAdapter(dataList)
                _dataLoaded.value = true
            }
        }
    }
}