package es.clcarras.mydues.viewmodel

import android.annotation.SuppressLint
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.*
import es.clcarras.mydues.adapter.DuesHomeAdapter
import es.clcarras.mydues.database.MyDuesDao
import es.clcarras.mydues.model.MyDues
import es.clcarras.mydues.ui.DuesDetailsDialogFragment
import es.clcarras.mydues.utils.Utility
import kotlinx.coroutines.launch

/**
 * ViewModel del Fragment de la vista Home
 */
class HomeViewModel(
    private val recurrences: Array<String> // Listado de recurrencias
) : ViewModel() {

    /**
     * Clase Factory del ViewModel, usado para pasar parámetros al mismo
     */
    class Factory(
        private val recurrences: Array<String>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            HomeViewModel(recurrences) as T
    }

    // LiveData que actúa como bandera para saber si se debe habilitar el launcher
    private var _launcherEnabled = MutableLiveData(false)
    val launcherEnabled: LiveData<Boolean> get() = _launcherEnabled

    // LiveData para saber si se ha borrado una cuota
    private val _deleted = MutableLiveData(false)
    val deleted: LiveData<Boolean> get() = _deleted

    // LiveData para saber si existen cuotas
    private val _noDues = MutableLiveData(true)
    val noDues: LiveData<Boolean> get() = _noDues

    // LiveData para almacenar el precio total
    private val _totalPrice = MutableLiveData(0.0)
    val totalPrice: LiveData<Double> get() = _totalPrice

    // LiveData para almacenar la recurrencia mostrada
    private val _recurrence = MutableLiveData(recurrences[2])
    val recurrence: LiveData<String> get() = _recurrence

    // Listado que se cargará en el adapter
    private var adapterDataList = mutableListOf<MyDues>()

    // Listado en el que se almacenarán todas las cuotas
    private var dataList = mutableListOf<MyDues>()

    // Adapter del listado de cuotas
    private var _adapter: DuesHomeAdapter? = null
    val adapter get() = _adapter

    // Cuadro de diálogo de detalles de la incidencia seleccionada en el listado
    var detailsDialogFragment: DuesDetailsDialogFragment? = null

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

            adapter!!.notifyDataSetChanged()  // Se notifica al adapter que la lista ha cambiado

            return false
        }

    }

    init {
        // Se inicializa el adapter con la lista vacía
        _adapter = DuesHomeAdapter(adapterDataList, recurrence.value!!)
        viewModelScope.launch {
            // Se obtienen todas las cuotas del usuario
            MyDuesDao().getMyDues().addOnSuccessListener { col ->
                // Se añade y notifica por cada cuota que haya en la colección
                for (doc in col) {
                    val myDues = doc.toObject(MyDues::class.java)
                    dataList.add(myDues)
                    adapterDataList.add(myDues)
                    _adapter!!.notifyItemInserted(adapterDataList.indexOf(myDues))
                }
                _noDues.value = dataList.isEmpty()
            }.addOnCompleteListener {
                // Una vez recorrida la lista se calcula el precio total
                adapterDataList.forEach {
                    _totalPrice.value =
                        _totalPrice.value?.plus(
                            Utility.calculateMonthlyPrice(it, recurrences)
                        )
                }
                checkPreloadDues()
            }
        }
    }

    /**
     * Método que elimina una cuota del listado
     */
    fun deleteDues() {
        // Se actualiza el adapter
        val i = adapterDataList.indexOf(adapter!!.selectedMyDues.value!!)
        adapterDataList.remove(adapter!!.selectedMyDues.value!!)
        dataList.remove(adapter!!.selectedMyDues.value!!)
        _adapter?.notifyItemRemoved(i)

        // Se levanta la bandera
        _deleted.value = true

        // Se recarga el precio
        reloadTotalPrice()

        // Se comprueba si queda alguna cuota precargada
        checkPreloadDues()

        _noDues.value = dataList.isEmpty()
    }

    /**
     * Método que baja de la bandera de eliminación
     */
    fun onDeleteComplete() {
        _deleted.value = false
    }

    /**
     * Método que comprueba si hay alguna cuota precargada en el listado
     */
    private fun checkPreloadDues() {
        viewModelScope.launch {
            dataList.forEach {
                // Si hay una cuota precargada se levanta la bandera que habilita el launcher
                if (!it.`package`.isNullOrBlank())
                    _launcherEnabled.value = true
            }
        }
    }

    /**
     * Método que actualiza la cuota del listado
     */
    fun updateDues() {
        val updatedDues = adapter?.selectedMyDues?.value!!
        _adapter?.notifyItemChanged(adapterDataList.indexOf(updatedDues))
        reloadTotalPrice()
    }

    /**
     * Método que calcula el precio total del listado
     */
    private fun reloadTotalPrice() {
        _totalPrice.value = 0.0
        adapterDataList.forEach {
            _totalPrice.value = _totalPrice.value?.plus(
                Utility.calculatePrice(_recurrence.value!!, it, recurrences)
            )
        }
    }

    /**
     * Método que cambia la recurrencia a la siguiente del array de recurrencias
     */
    @SuppressLint("NotifyDataSetChanged")
    fun changeRecurrence() {
        _recurrence.value = when (recurrence.value!!) {
            recurrences[0] -> recurrences[1]
            recurrences[1] -> recurrences[2]
            recurrences[2] -> recurrences[3]
            else -> recurrences[0]
        }
        // Se cambia la recurrencia en el adapter y se notifica
        _adapter!!.setRecurrence(_recurrence.value!!)
        _adapter!!.notifyDataSetChanged()
        reloadTotalPrice()
    }

}