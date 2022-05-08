package es.clcarras.mydues.ui.dialogs.dues_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.model.Dues
import es.clcarras.mydues.ui.home.HomeViewModel

class DuesDetailsDialogViewModelFactory(
    private val db: DuesRoomDatabase,
    private val dues: Dues?,
    private val homeViewModel: HomeViewModel?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DuesDetailsDialogViewModel(db, dues!!, homeViewModel!!) as T
    }
}