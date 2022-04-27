package es.clcarras.mydues.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.databinding.HomeFragmentBinding
import es.clcarras.mydues.model.Dues
import es.clcarras.mydues.database.DuesRoomDatabase
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private var _db: DuesRoomDatabase? = null
    private val db get() = _db!!

    private var dataList = mutableListOf<Dues>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).getFab()?.show()
        _db = DuesRoomDatabase.getDatabase(requireContext())
        readDatabase()
    }

    private fun readDatabase() {
        lifecycleScope.launch {
            with(db.duesDao()) { dataList = getAll() }
            with(binding.recyclerView) {
                layoutManager = GridLayoutManager(requireContext(), GridLayoutManager.VERTICAL)
                adapter = DuesAdapter(this@HomeFragment, dataList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun deleteDues(dues: Dues) {
        lifecycleScope.launch {
            db.duesDao().remove(dues)
            val i = dataList.indexOf(dues)
            dataList.remove(dues)
            binding.recyclerView.adapter?.notifyItemRemoved(i)
        }
    }

    fun updateDues(dues: Dues) {
        lifecycleScope.launch {
            db.duesDao().update(dues)
            binding.recyclerView.adapter?.notifyItemChanged(dataList.indexOf(dues))
        }
    }
}