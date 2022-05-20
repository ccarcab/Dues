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

class DuesHomeAdapter(
    private val dataSet: List<MyDues>,
    private var currentRecurrence: String
) : RecyclerView.Adapter<DuesHomeAdapter.ViewHolder>() {

    private val _selectedMyDues = MutableLiveData<MyDues?>(null)
    val selectedMyDues: LiveData<MyDues?> get() = _selectedMyDues

    inner class ViewHolder(val binding: ItemDuesHomeBinding, val recurrences: Array<String>) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDuesHomeBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(
            binding,
            parent.context.resources.getStringArray(R.array.recurrence_array)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(dataSet[position]) {
                val textColor = Utility.contrastColor(cardColor)

                tvName.text = name
                tvName.setTextColor(textColor)

                val price: Double = Utility.calculatePrice(
                    currentRecurrence, this, holder.recurrences
                )

                tvPrice.text = String.format("%.2f", price)
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

    fun setRecurrence(recurrence: String) {
        currentRecurrence = recurrence
    }

}