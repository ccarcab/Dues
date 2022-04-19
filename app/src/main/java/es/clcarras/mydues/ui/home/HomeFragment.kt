package es.clcarras.mydues.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import es.clcarras.mydues.databinding.HomeFragmentBinding
import es.clcarras.mydues.model.Due
import es.clcarras.mydues.database.DueRoomDatabase
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel

    private var dataList: List<Due> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = HomeFragmentBinding.inflate(inflater, container, false)

        initDatabase()

        return binding.root
    }

    private fun initDatabase() {
        lifecycleScope.launch {
            val db = DueRoomDatabase.getDatabase(requireContext())

            with(db.dueDao()) {
                if (getDueCount() <= 0) {
                    Log.i("Database", "Datos insertados en la base de datos")
                    insertAll(
                        arrayListOf(
                            Due(name = "Netflix", date = Date.from(Instant.now()), price = "9€"),
                            Due(name = "Amazon", date = Date.from(Instant.now()), price = "9€"),
                            Due(name = "Disney", date = Date.from(Instant.now()), price = "9€"),
                            Due(name = "O2", date = Date.from(Instant.now()), price = "9€"),
                            Due(name = "GamePass", date = Date.from(Instant.now()), price = "9€")
                        )
                    )
                }
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