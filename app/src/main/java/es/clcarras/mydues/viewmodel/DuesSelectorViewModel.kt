package es.clcarras.mydues.viewmodel

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import es.clcarras.mydues.adapter.DuesSelectorAdapter
import es.clcarras.mydues.database.PreloadDuesDao
import es.clcarras.mydues.model.PreloadedDues
import kotlinx.coroutines.launch

class DuesSelectorViewModel : ViewModel() {

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
                if (it.name!!.contains(newText!!, true))
                    adapterDataList.add(it)
            }

            adapter!!.notifyDataSetChanged()

            return false
        }

    }

    init {
        _adapter = DuesSelectorAdapter(adapterDataList)
        viewModelScope.launch {
            PreloadDuesDao().getAllPreloadDues().addOnSuccessListener { col ->
                for (doc in col) {
                    val preDues = doc.toObject(PreloadedDues::class.java)
                    dataList.add(preDues)
                    adapterDataList.add(preDues)
                    _adapter!!.notifyItemInserted(adapterDataList.indexOf(preDues))
                }
            }
        }
    }
}

