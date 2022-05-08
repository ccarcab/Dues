package es.clcarras.mydues.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.clcarras.mydues.database.DuesRoomDatabase

class HomeViewModelFactory(
    private val database: DuesRoomDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(database) as T
    }
}