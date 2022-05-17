package es.clcarras.mydues.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import es.clcarras.mydues.utils.Utility
import es.clcarras.mydues.databinding.DuesHomeItemBinding
import es.clcarras.mydues.model.MyDues

class DuesHomeAdapter(
    private val dataSet: List<MyDues>
) : RecyclerView.Adapter<DuesHomeAdapter.ViewHolder>() {

    private val _selectedMyDues = MutableLiveData<MyDues?>(null)
    val selectedMyDues: LiveData<MyDues?> get() = _selectedMyDues

    inner class ViewHolder(val binding: DuesHomeItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DuesHomeItemBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(dataSet[position]) {
                val textColor = Utility.contrastColor(cardColor)

                tvName.text = name
                tvName.setTextColor(textColor)

                tvPrice.text = price.toString()
                tvPrice.setTextColor(textColor)

                tvCurrency.setTextColor(textColor)

                container.setCardBackgroundColor(cardColor)
                container.setOnClickListener { _selectedMyDues.value = this }
            }
        }
    }

    override fun getItemCount(): Int = dataSet.size

    fun unSelectDues() {
        notifyItemChanged(dataSet.indexOf(_selectedMyDues.value))
        _selectedMyDues.value = null
    }

}