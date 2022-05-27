package es.clcarras.mydues.viewmodel

import androidx.lifecycle.*
import es.clcarras.mydues.adapter.PreloadDuesAdapter
import es.clcarras.mydues.database.MyDuesDao
import es.clcarras.mydues.database.PreloadDuesDao
import es.clcarras.mydues.model.MyDues
import es.clcarras.mydues.model.PreloadedDues
import kotlinx.coroutines.launch

/**
 * ViewModel del Fragment de la vista de Launcher
 */
class PreloadDuesBottomSheetViewModel : ViewModel() {

    // Adpater del listado de cuotas precargadas
    private var _adapter: PreloadDuesAdapter? = null
    val adapter get() = _adapter

    // Listado que se motrará en el adapter
    private var dataList = mutableListOf<PreloadedDues>()

    init {
        // Se inicializa el adapter con la lista vacía
        _adapter = PreloadDuesAdapter(dataList)
        viewModelScope.launch {
            // Listado auxiliar
            val appsAdded = mutableListOf<String>()
            // Se obtienen todas las cuotas del usuario
            MyDuesDao().getMyDues().addOnSuccessListener { col ->
                // Se añade y notifica por cada cuota que se haya añadido
                for (doc in col) {
                    val dues = doc.toObject(MyDues::class.java)
                    // Se añadirá al listado si tiene nombre de paquete y no esté ya añadida
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