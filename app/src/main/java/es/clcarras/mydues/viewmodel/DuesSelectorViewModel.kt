package es.clcarras.mydues.viewmodel

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import es.clcarras.mydues.adapter.DuesSelectorAdapter
import es.clcarras.mydues.model.PreloadedDues

class DuesSelectorViewModel(
    firestore: FirebaseFirestore
) : ViewModel() {

    class Factory(private val firestore: FirebaseFirestore) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DuesSelectorViewModel(firestore) as T
    }

    private val _loadComplete = MutableLiveData(false)
    val loadComplete: LiveData<Boolean> get() = _loadComplete

    private var adapterDataList = mutableListOf<PreloadedDues>()
    private var dataList = mutableListOf<PreloadedDues>()

    private var _adapter: DuesSelectorAdapter? = null
    val adapter get() = _adapter

    val onQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?) = false

        @SuppressLint("NotifyDataSetChanged")
        override fun onQueryTextChange(newText: String?): Boolean {

            adapterDataList.clear()

            dataList.forEach {
                if (it.name.contains(newText!!, true))
                    adapterDataList.add(it)
            }

            adapter!!.notifyDataSetChanged()

            return false
        }

    }

    init {
        adapterDataList.clear()
        firestore
            .collection("dues")
            .get()
            .addOnSuccessListener { docs ->
                for (doc in docs) {
                    dataList.add(
                        PreloadedDues(
                            doc["name"].toString(),
                            doc["color"].toString(),
                            Uri.parse(doc["image"].toString()),
                            doc["package"].toString()
                        )
                    )
                }

                adapterDataList.addAll(dataList)
                _adapter = DuesSelectorAdapter(adapterDataList)
                _loadComplete.value = true
            }

    }
}

