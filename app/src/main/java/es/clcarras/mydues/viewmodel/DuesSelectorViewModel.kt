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
            .get()
            .addOnSuccessListener { docs ->
                val list = mutableListOf<PreloadedDues>()
                for (doc in docs) {
                    list.add(
                        PreloadedDues(
                            doc["name"].toString(),
                            doc["color"].toString(),
                            Uri.parse(doc["image"].toString()),
                            doc["package"].toString()
                        )
                    )
                }

                _adapter = DuesSelectorAdapter(list)
                _loadComplete.value = true
            }

    }
}

