package es.clcarras.mydues.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.clcarras.mydues.databinding.DueRowItemBinding
import es.clcarras.mydues.model.Due
import java.util.*

class DuesAdapter(private val dataSet: List<Due>):
    RecyclerView.Adapter<DuesAdapter.ViewHolder>() {

    class ViewHolder(val binding: DueRowItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DueRowItemBinding
            .inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rnd = Random()
        val color: Int = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        with(holder.binding) {
            with(dataSet[position]) {
                tvName.text = name
                tvPrice.text = "$price €"
            }
            container.setCardBackgroundColor(color)
        }
    }

    override fun getItemCount(): Int = dataSet.size
}