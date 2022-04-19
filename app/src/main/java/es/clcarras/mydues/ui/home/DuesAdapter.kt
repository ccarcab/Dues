package es.clcarras.mydues.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.clcarras.mydues.databinding.DueRowItemBinding
import es.clcarras.mydues.model.Due
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        with(holder.binding) {
            with(dataSet[position]) {
                tvName.text = name
                tvDate.text = formatter.format(date)
                tvPrice.text = price
            }
        }
    }

    override fun getItemCount(): Int = dataSet.size
}