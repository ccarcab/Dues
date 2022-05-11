package es.clcarras.mydues.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.DuesSelectorFragmentBinding
import es.clcarras.mydues.viewmodel.DuesSelectorViewModel

class DuesSelectorFragment: Fragment() {

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

        return binding.root
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
        requireActivity().findViewById<FloatingActionButton>(R.id.fab).hide()
    }

}