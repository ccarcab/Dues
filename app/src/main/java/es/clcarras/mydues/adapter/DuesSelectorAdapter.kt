package es.clcarras.mydues.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import es.clcarras.mydues.databinding.ItemDuesSelectorBinding
import es.clcarras.mydues.model.PreloadedDues
import es.clcarras.mydues.ui.DuesSelectorFragmentDirections
import es.clcarras.mydues.utils.Utility

/**
 * Clase Adapter del listado de cuotas que aparecen en la ventana de selección de cuota
 */
class DuesSelectorAdapter(
    private val dataList: List<PreloadedDues> // Listado de cuotas precargadas
) : RecyclerView.Adapter<DuesSelectorAdapter.ViewHolder>() {

    /**
     * Clase interna ViewHolder, encargada de almacenar una instancia de la vista de cada elemento
     */
    inner class ViewHolder(
        val binding: ItemDuesSelectorBinding // Objeto de enlace de la vista con el código
    ) : RecyclerView.ViewHolder(binding.root)

    /**
     * Método que infla la vista que se asignará a cada elemento y crea el ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDuesSelectorBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    /**
     * Método que se llama una vez por cada elemento que hay en la lista recibida, el cuál define
     * el comportamiento de cada elemento
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) { // With para usar el binding
            with(dataList[position]) { // With para usar el elemento en la posición X

                // Se carga la imagen en el imageview usando la librería Picasso
                Picasso.get().load(image).into(ivDuesImage)

                // Se muestra el nombre de la cuota
                tvDuesName.text = name

                // Se asignan los colores
                tvDuesName.setTextColor(Utility.contrastColor(color))
                container.setCardBackgroundColor(color)
                ivDuesImage.setColorFilter(Utility.contrastColor(color))

                // Se asigna el onclick al elemento contenedor
                container.setOnClickListener {
                    val action = DuesSelectorFragmentDirections
                        .actionNavDuesSelectorToNavNewDue(`package`!!)
                    it.findNavController().navigate(action)
                }
            }
        }
    }

    /**
     * Método que devuelve el total de elementos en la lista recibida por el adapter
     */
    override fun getItemCount(): Int = dataList.size

}