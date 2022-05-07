package es.clcarras.mydues.ui.new_due

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.clcarras.mydues.database.DuesRoomDatabase

class NewDuesViewModelFactory(
    private val db: DuesRoomDatabase,
    private val cardColor: Int,
    private val contrastColor: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewDuesViewModel(db, cardColor, contrastColor) as T
    }
}