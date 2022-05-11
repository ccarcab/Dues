package es.clcarras.mydues.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import es.clcarras.mydues.databinding.DuesSelectorItemBinding
import es.clcarras.mydues.model.PreloadedDues

class DuesSelectorAdapter(
    private val dataList: List<PreloadedDues>
) : RecyclerView.Adapter<DuesSelectorAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: DuesSelectorItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DuesSelectorItemBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(dataList[position]) {
                Picasso.get().load(image)
                    .resize(250, 250)
                    .into(ivDuesImage)
                tvDuesName.text = name
            }
        }
    }

    override fun getItemCount(): Int = dataList.size


}