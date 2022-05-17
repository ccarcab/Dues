package es.clcarras.mydues.viewmodel

import androidx.lifecycle.*
import es.clcarras.mydues.adapter.PreloadDuesAdapter
import es.clcarras.mydues.database.MyDuesDao
import es.clcarras.mydues.database.PreloadDuesDao
import es.clcarras.mydues.model.MyDues
import es.clcarras.mydues.model.PreloadedDues
import kotlinx.coroutines.launch

class PreloadDuesBottomSheetViewModel : ViewModel() {

    private var _adapter: PreloadDuesAdapter? = null
    val adapter get() = _adapter

    private var dataList = mutableListOf<PreloadedDues>()

    init {
        _adapter = PreloadDuesAdapter(dataList)
        viewModelScope.launch {
            val appsAdded = mutableListOf<String>()
            MyDuesDao().getAllDocs().addOnSuccessListener { col ->
                for (doc in col) {
                    val dues = doc.toObject(MyDues::class.java)
                    if (!dues.`package`.isNullOrBlank() && !appsAdded.contains(dues.`package`)) {
                        appsAdded.add(dues.`package`!!)
                        PreloadDuesDao().getPreloadDueByPackage(dues.`package`!!).addOnSuccessListener {
                            val preDues = it.single().toObject(PreloadedDues::class.java)
                            dataList.add(preDues)
                            _adapter!!.notifyItemInserted(dataList.indexOf(preDues))
                        }
                    }
                }
            }
        }
    }

}