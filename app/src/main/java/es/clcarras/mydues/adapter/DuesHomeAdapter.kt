package es.clcarras.mydues.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.ItemDuesHomeBinding
import es.clcarras.mydues.model.MyDues
import es.clcarras.mydues.utils.Utility

/**
 * Clase Adapter del listado de cuotas que aparecen en la ventana principal de la aplicación
 */
class DuesHomeAdapter(
    private val dataSet: List<MyDues>, // Listado de cuotas que se encargará de cargar el adapter
    private var currentRecurrence: String // Recurrencia inicial
) : RecyclerView.Adapter<DuesHomeAdapter.ViewHolder>() {

    // LiveData usado como trigger para mostrar los detalles de la cuota pulsada
    private val _selectedMyDues = MutableLiveData<MyDues?>(null)
    val selectedMyDues: LiveData<MyDues?> get() = _selectedMyDues

    /**
     * Clase interna ViewHolder, encargada de almacenar una instancia de la vista de cada elemento
     */
    inner class ViewHolder(
        val binding: ItemDuesHomeBinding, // Objeto de enlace de la vista con el código
        val recurrences: Array<String> // Listado de recurrencias que existen en la aplicación
    ) : RecyclerView.ViewHolder(binding.root)

    /**
     * Método que infla la vista que se asignará a cada elemento y crea el ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDuesHomeBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(
            binding,
            parent.context.resources.getStringArray(R.array.recurrence_array)
        )
    }

    /**
     * Método que se llama una vez por cada elemento que hay en la lista recibida, el cuál define
     * el comportamiento de cada elemento
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) { // With para usar el binding
            with(dataSet[position]) { // With para usar el elemento en la posición X

                // Se obtiene el color que tendrá el texto
                val textColor = Utility.contrastColor(cardColor)

                // Se muestra el nombre y se le asigna un color
                tvName.text = name

                // Se calcula el precio que se mostrará en función de la recurrencia actual,
                // la cuota y las recurrencias disponibles
                val price: Double = Utility.calculatePrice(
                    currentRecurrence, this, holder.recurrences
                )

                // Se formatea el precio para mostrar solo dos decimales
                tvPrice.text = String.format("%.2f", price)

                // Se establece el color del texto
                tvPrice.setTextColor(textColor)
                tvCurrency.setTextColor(textColor)
                tvName.setTextColor(textColor)

                // Se le asigna el color al contenedor y su onclick
                container.setCardBackgroundColor(cardColor)
                container.setOnClickListener { _selectedMyDues.value = this }
            }
        }
    }

    /**
     * Método que devuelve el total de elementos en la lista recibida por el adapter
     */
    override fun getItemCount(): Int = dataSet.size

    /**
     * Método usado para deseleccionar la cuota cuando se cierra el diálogo de detalles o se borra
     */
    fun unSelectDues() {
        notifyItemChanged(dataSet.indexOf(_selectedMyDues.value))
        _selectedMyDues.value = null
    }

    /**
     * Método para cambiar la recurrencia actual
     */
    fun setRecurrence(recurrence: String) {
        currentRecurrence = recurrence
    }

}