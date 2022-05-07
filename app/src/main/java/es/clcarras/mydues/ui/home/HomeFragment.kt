package es.clcarras.mydues.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.HomeFragmentBinding
import es.clcarras.mydues.model.Dues
import es.clcarras.mydues.database.DuesRoomDatabase
import kotlinx.coroutines.launch

// TODO: Hay que quitar la referencia del fragment cuando se crean otras vistas, si no crashea

class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var snackbar: Snackbar

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
        _db = DuesRoomDatabase.getDatabase(requireContext())
        readDatabase()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        with(requireActivity().findViewById<FloatingActionButton>(R.id.fab)) {
            setImageResource(android.R.drawable.ic_menu_add)
            setOnClickListener { findNavController().navigate(R.id.nav_new_due) }
            snackbar = Snackbar.make(this, "", Snackbar.LENGTH_LONG).apply {
                anchorView = this@with
            }
        }
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
        val i = dataList.indexOf(dues)
        dataList.remove(dues)
        binding.recyclerView.adapter?.notifyItemRemoved(i)
        snackbar.apply { setText("Dues Deleted!") }.show()
    }

    fun updateDues(dues: Dues) {
        binding.recyclerView.adapter?.notifyItemChanged(dataList.indexOf(dues))
    }
}