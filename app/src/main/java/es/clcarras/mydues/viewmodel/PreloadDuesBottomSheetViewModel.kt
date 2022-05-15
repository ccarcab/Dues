package es.clcarras.mydues.viewmodel

import androidx.lifecycle.*
import es.clcarras.mydues.adapter.PreloadDuesAdapter
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.model.MyDues
import kotlinx.coroutines.launch

class PreloadDuesBottomSheetViewModel(
    private val database: DuesRoomDatabase
): ViewModel() {

    class Factory(
        private val database: DuesRoomDatabase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            PreloadDuesBottomSheetViewModel(database) as T
    }

    private val _dataLoaded = MutableLiveData(false)
    val dataLoaded: LiveData<Boolean> get() = _dataLoaded

    private var _adapter: PreloadDuesAdapter? = null
    val adapter get() = _adapter

    private var dataList = mutableListOf<MyDues>()

    init {
        viewModelScope.launch {
            with(database.duesDao()) {
                dataList = getPreloadDues()
                dataList = dataList.toSet().toMutableList()
                _adapter = PreloadDuesAdapter(dataList)
                _dataLoaded.value = true
            }
        }
    }

}