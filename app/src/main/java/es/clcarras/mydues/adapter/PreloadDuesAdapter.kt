package es.clcarras.mydues.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import es.clcarras.mydues.databinding.ItemPreloadDuesBinding
import es.clcarras.mydues.model.PreloadedDues
import es.clcarras.mydues.utils.Utility

class PreloadDuesAdapter(
    private val dataList: List<PreloadedDues>
) : RecyclerView.Adapter<PreloadDuesAdapter.ViewHolder>() {

    private val _selectedAppPackage = MutableLiveData("")
    val selectedAppPackage: LiveData<String> get() = _selectedAppPackage

    inner class ViewHolder(val binding: ItemPreloadDuesBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPreloadDuesBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            with(dataList[position]) {
                Picasso.get().load(image).into(ivLogo)
                ivLogo.setOnClickListener {
                    _selectedAppPackage.value = `package`
                }
                ivLogo.setColorFilter(Utility.contrastColor(color))
                container.setCardBackgroundColor(color)
            }
        }
    }

    override fun getItemCount() = dataList.size

    fun onAppOpen() {
        _selectedAppPackage.value = ""
    }
}