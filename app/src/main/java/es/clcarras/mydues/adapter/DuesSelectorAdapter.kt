package es.clcarras.mydues.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import es.clcarras.mydues.databinding.DuesSelectorItemBinding
import es.clcarras.mydues.model.PreloadedDues
import es.clcarras.mydues.ui.DuesSelectorFragmentDirections
import es.clcarras.mydues.utils.Utility

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
//                    .resize(250, 250)
                    .into(ivDuesImage)
                tvDuesName.text = name

                val cardColor = Color.parseColor("#FF$color")
                tvDuesName.setTextColor(Utility.contrastColor(cardColor))
                container.setCardBackgroundColor(cardColor)
                ivDuesImage.setColorFilter(Utility.contrastColor(cardColor))
                container.setOnClickListener {
                    val action = DuesSelectorFragmentDirections.actionNavDuesSelectorToNavNewDue(
                        name, color, image.toString()
                    )
                    it.findNavController().navigate(action)
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size


}