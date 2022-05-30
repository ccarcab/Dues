package es.clcarras.mydues.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import es.clcarras.mydues.databinding.ItemPreloadDuesBinding
import es.clcarras.mydues.model.PreloadedDues
import es.clcarras.mydues.utils.Utility

/**
 * Clase Adapter del listado de cuotas que aparecen en la vista launcher
 */
class PreloadDuesAdapter(
    private val dataList: List<PreloadedDues> // Listado de cuotas precargadas
) : RecyclerView.Adapter<PreloadDuesAdapter.ViewHolder>() {

    // LiveData usado como trigger para abrir la aplicación de la cuota pulsada
    private val _selectedAppPackage = MutableLiveData("")
    val selectedAppPackage: LiveData<String> get() = _selectedAppPackage

    /**
     * Clase interna ViewHolder, encargada de almacenar una instancia de la vista de cada elemento
     */
    inner class ViewHolder(
        val binding: ItemPreloadDuesBinding // Objeto de enlace de la vista con el código
    ) : RecyclerView.ViewHolder(binding.root)

    /**
     * Método que infla la vista que se asignará a cada elemento y crea el ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPreloadDuesBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    /**
     * Método que se llama una vez por cada elemento que hay en la lista recibida, el cuál define
     * el comportamiento de cada elemento
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) { // With para usar el binding
            with(dataList[position]) { // With para usar el elemento en la posición X

                // Se carga la imagen en el imageview
                Picasso.get().load(image).into(ivLogo)

                // Se establece el onclick al imageview
                ivLogo.setOnClickListener { _selectedAppPackage.value = `package` }

                // Se le asigna color a la imagen en función del color de tarjeta
                ivLogo.setColorFilter(Utility.contrastColor(color))

                // Se asigna el color al contenedor
                container.setCardBackgroundColor(color)
            }
        }
    }

    /**
     * Método que devuelve el total de elementos en la lista recibida por el adapter
     */
    override fun getItemCount() = dataList.size

    /**
     * Método usado para deseleccionar una vez abierta la aplicación
     */
    fun onAppOpen() {
        _selectedAppPackage.value = ""
    }
}