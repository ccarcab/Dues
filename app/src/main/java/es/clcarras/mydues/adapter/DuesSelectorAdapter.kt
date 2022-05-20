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

class DuesSelectorAdapter(
    private val dataList: List<PreloadedDues>
) : RecyclerView.Adapter<DuesSelectorAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDuesSelectorBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDuesSelectorBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(dataList[position]) {
                Picasso.get().load(image).into(ivDuesImage)
                tvDuesName.text = name
                tvDuesName.setTextColor(Utility.contrastColor(color))
                container.setCardBackgroundColor(color)
                ivDuesImage.setColorFilter(Utility.contrastColor(color))
                container.setOnClickListener {
                    val action = DuesSelectorFragmentDirections
                        .actionNavDuesSelectorToNavNewDue(`package`!!)
                    it.findNavController().navigate(action)
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size


}