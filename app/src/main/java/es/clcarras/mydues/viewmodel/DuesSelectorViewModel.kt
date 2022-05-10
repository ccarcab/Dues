package es.clcarras.mydues.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import es.clcarras.mydues.adapter.DuesSelectorAdapter
import es.clcarras.mydues.model.PreloadedDues

class DuesSelectorViewModel(
    firestore: FirebaseFirestore
) : ViewModel() {

    class Factory(private val firestore: FirebaseFirestore) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DuesSelectorViewModel(firestore) as T
        }
    }

    private val _loadComplete = MutableLiveData(false)
    val loadComplete: LiveData<Boolean> get() = _loadComplete

    private var _adapter: DuesSelectorAdapter? = null
    val adapter get() = _adapter

    init {
        firestore
            .collection("dues")
            .document("preload_dues")
            .get()
            .addOnSuccessListener { doc ->
                val list = mutableListOf<PreloadedDues>()
                with((doc.data as Map<*, *>)) {
                    forEach {
                        list.add(
                            PreloadedDues(
                                it.key.toString(),
                                Uri.parse(it.value.toString())
                            )
                        )
                    }
                }
                _adapter = DuesSelectorAdapter(list)
                _loadComplete.value = true
            }
    }

}