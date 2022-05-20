package es.clcarras.mydues.viewmodel

import android.annotation.SuppressLint
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.*
import es.clcarras.mydues.adapter.DuesHomeAdapter
import es.clcarras.mydues.database.MyDuesDao
import es.clcarras.mydues.model.MyDues
import es.clcarras.mydues.ui.DuesDetailsDialogFragment
import es.clcarras.mydues.utils.Utility
import kotlinx.coroutines.launch

class HomeViewModel(
    private val recurrences: Array<String>
) : ViewModel() {

    class Factory(
        private val recurrences: Array<String>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            HomeViewModel(recurrences) as T
    }

    private var _launcherEnabled = MutableLiveData(false)
    val launcherEnabled: LiveData<Boolean> get() = _launcherEnabled

    private val _deleted = MutableLiveData(false)
    val deleted: LiveData<Boolean> get() = _deleted

    private val _totalPrice = MutableLiveData(0.0)
    val totalPrice: LiveData<Double> get() = _totalPrice

    private val _recurrence = MutableLiveData(recurrences[2])
    val recurrence: LiveData<String> get() = _recurrence

    private var adapterDataList = mutableListOf<MyDues>()
    private var dataList = mutableListOf<MyDues>()

    private var _adapter: DuesHomeAdapter? = null
    val adapter get() = _adapter

    var detailsDialogFragment: DuesDetailsDialogFragment? = null

    val onQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?) = false

        @SuppressLint("NotifyDataSetChanged")
        override fun onQueryTextChange(newText: String?): Boolean {

            adapterDataList.clear()

            dataList.forEach {
                if (it.name!!.contains(newText!!, true))
                    adapterDataList.add(it)
            }

            adapter!!.notifyDataSetChanged()

            return false
        }

    }

    init {
        _adapter = DuesHomeAdapter(adapterDataList, recurrence.value!!)
        viewModelScope.launch {
            MyDuesDao().getMyDues().addOnSuccessListener { col ->
                for (doc in col) {
                    val myDues = doc.toObject(MyDues::class.java)
                    dataList.add(myDues)
                    adapterDataList.add(myDues)
                    _adapter!!.notifyItemInserted(adapterDataList.indexOf(myDues))
                }
            }.addOnCompleteListener {
                adapterDataList.forEach {
                    _totalPrice.value =
                        _totalPrice.value?.plus(
                            Utility.calculateMonthlyPrice(it, recurrences).toInt()
                        )
                }
                checkPreloadDues()
            }
        }
    }

    fun deleteDues() {
        val i = adapterDataList.indexOf(adapter!!.selectedMyDues.value!!)
        adapterDataList.remove(adapter!!.selectedMyDues.value!!)
        dataList.remove(adapter!!.selectedMyDues.value!!)
        _adapter?.notifyItemRemoved(i)
        _deleted.value = true
        reloadTotalPrice()
        checkPreloadDues()
    }

    private fun checkPreloadDues() {
        viewModelScope.launch {
            dataList.forEach {
                if (!it.`package`.isNullOrBlank())
                    _launcherEnabled.value = true
            }
        }
    }

    fun updateDues() {
        val updatedDues = adapter?.selectedMyDues?.value!!
        _adapter?.notifyItemChanged(adapterDataList.indexOf(updatedDues))
        reloadTotalPrice()
    }

    private fun reloadTotalPrice() {
        _totalPrice.value = 0.0
        adapterDataList.forEach {
            _totalPrice.value = _totalPrice.value?.plus(
                Utility.calculatePrice(_recurrence.value!!, it, recurrences)
            )
        }
    }

    fun onDeleteComplete() {
        _deleted.value = false
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeRecurrence() {
        _recurrence.value = when (recurrence.value!!) {
            recurrences[0] -> recurrences[1]
            recurrences[1] -> recurrences[2]
            recurrences[2] -> recurrences[3]
            else -> recurrences[0]
        }
        _adapter!!.setRecurrence(_recurrence.value!!)
        _adapter!!.notifyDataSetChanged()
        reloadTotalPrice()
    }

}