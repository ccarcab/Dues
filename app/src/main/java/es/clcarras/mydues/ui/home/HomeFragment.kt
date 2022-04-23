package es.clcarras.mydues.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

    private lateinit var viewModel: HomeViewModel

    private var dataList: List<Dues> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).getFab()?.show()
        readDatabase()
    }

    private fun readDatabase() {
        lifecycleScope.launch {
            val db = DuesRoomDatabase.getDatabase(requireContext())

            with(db.dueDao()) {
                dataList = getAll()
            }

            with(binding.recyclerView) {
                layoutManager = GridLayoutManager(requireContext(), GridLayoutManager.VERTICAL)
                adapter = DuesAdapter(dataList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}