package es.clcarras.mydues.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.clcarras.mydues.Utility
import es.clcarras.mydues.databinding.DueRowItemBinding
import es.clcarras.mydues.model.Dues
import java.util.*

class DuesAdapter(private val dataSet: List<Dues>):
    RecyclerView.Adapter<DuesAdapter.ViewHolder>() {

    class ViewHolder(val binding: DueRowItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DueRowItemBinding
            .inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(dataSet[position]) {
                val textColor = Utility.contrastColor(cardColor)
                tvName.text = name
                tvName.setTextColor(textColor)
                tvPrice.text = "$price €"
                tvPrice.setTextColor(textColor)
                container.setCardBackgroundColor(cardColor)
            }
        }
    }

    override fun getItemCount(): Int = dataSet.size
}