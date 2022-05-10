package es.clcarras.mydues.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import es.clcarras.mydues.utils.Utility
import es.clcarras.mydues.databinding.DueRowItemBinding
import es.clcarras.mydues.database.Dues

class DuesAdapter(
    private val dataSet: List<Dues>
) : RecyclerView.Adapter<DuesAdapter.ViewHolder>() {

    private val _selectedDues = MutableLiveData<Dues?>(null)
    val selectedDues: LiveData<Dues?> get() = _selectedDues

    inner class ViewHolder(val binding: DueRowItemBinding) : RecyclerView.ViewHolder(binding.root)

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

                tvPrice.text = price
                tvPrice.setTextColor(textColor)

                container.setCardBackgroundColor(cardColor)
                container.setOnClickListener { _selectedDues.value = this }
            }
        }
    }

    override fun getItemCount(): Int = dataSet.size

    fun unSelectDues() {
        notifyItemChanged(dataSet.indexOf(_selectedDues.value))
        _selectedDues.value = null
    }

}