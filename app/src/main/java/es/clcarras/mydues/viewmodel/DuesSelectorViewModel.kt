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

/**
 * ViewModel del Fragment de selección de cuotas
 */
class DuesSelectorViewModel : ViewModel() {

    // Listado que se cargará en el adapter
    private var adapterDataList = mutableListOf<PreloadedDues>()

    // Listado en el que se almacenarán todas las cuotas precargadas
    private var dataList = mutableListOf<PreloadedDues>()

    // Adapter del listado de cuotas precargadas
    private var _adapter: DuesSelectorAdapter? = null
    val adapter get() = _adapter

    // Objeto que tiene un método que se ejecutará cuándo se escriba en la barra de búsqueda
    val onQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?) = false

        @SuppressLint("NotifyDataSetChanged")
        override fun onQueryTextChange(newText: String?): Boolean {
            // Al escribir en la barra de búsqueda:
            adapterDataList.clear() // Se vacía el listado mostrado

            // Se recorre el listado con todas las cuotas y se añaden las que cuyo
            // nombre coincidan con el criterio de búsqueda
            dataList.forEach {
                if (it.name!!.contains(newText!!, true))
                    adapterDataList.add(it)
            }
            adapter!!.notifyDataSetChanged() // Se notifica al adapter que la lista ha cambiado

            return false
        }

    }

    init {
        // Se inicializa el adapter con la lista vacía
        _adapter = DuesSelectorAdapter(adapterDataList)
        viewModelScope.launch {
            // Se obtienen todas las cuotas precargadas
            PreloadDuesDao().getAllPreloadDues().addOnSuccessListener { col ->
                // Se añade y notifica por cada cuota que haya en la colección
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

