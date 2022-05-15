package es.clcarras.mydues.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.DuesSelectorFragmentBinding
import es.clcarras.mydues.viewmodel.DuesSelectorViewModel

class DuesSelectorFragment : Fragment() {

    private lateinit var binding: DuesSelectorFragmentBinding
    private lateinit var viewModel: DuesSelectorViewModel
    private lateinit var viewModelFactory: DuesSelectorViewModel.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DuesSelectorFragmentBinding.inflate(inflater, container, false)
        viewModelFactory = DuesSelectorViewModel.Factory(FirebaseFirestore.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[DuesSelectorViewModel::class.java]

        binding.btnNewDues.setOnClickListener {
            val action = DuesSelectorFragmentDirections.actionNavDuesSelectorToNavNewDue()
            findNavController().navigate(action)
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadComplete.observe(viewLifecycleOwner) {
            if (it)
                with(binding.rvDuesSelector) {
                    adapter = viewModel.adapter
                    layoutManager = GridLayoutManager(requireContext(), GridLayoutManager.VERTICAL)
                }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        (requireActivity() as MainActivity).getFab().hide()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bottom_app_bar, menu)

        val onActionListener = object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                binding.rvDuesSelector
                    .updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 1.0f }
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                binding.rvDuesSelector
                    .updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 0.0f }
                return true
            }
        }
        menu.findItem(R.id.filter).apply {
            setOnActionExpandListener(onActionListener)
            val searchView = actionView as SearchView
            searchView.queryHint = getString(R.string.filter_hint)
            searchView.setOnQueryTextListener(viewModel.onQueryTextListener)
        }

        menu.findItem(R.id.launcher).isVisible = false
    }

}