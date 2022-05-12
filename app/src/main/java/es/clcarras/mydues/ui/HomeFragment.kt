package es.clcarras.mydues.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import es.clcarras.mydues.R
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.databinding.HomeFragmentBinding
import es.clcarras.mydues.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private lateinit var binding: HomeFragmentBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var viewModelFactory: HomeViewModel.Factory

    private lateinit var snackbar: Snackbar

    private var lastTotal: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        viewModelFactory = HomeViewModel.Factory(
            DuesRoomDatabase.getDatabase(requireContext()),
            resources.getStringArray(R.array.recurrence_array)
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
        viewModel.loadDatabase()
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        setObservers()
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        with(requireActivity().findViewById<FloatingActionButton>(R.id.fab)) {
            setImageResource(android.R.drawable.ic_menu_add)
            setOnClickListener { findNavController().navigate(R.id.nav_dues_selector) }
            show()
            snackbar = Snackbar.make(this, "", Snackbar.LENGTH_LONG).apply {
                anchorView = this@with
            }
        }
    }

    private fun setObservers() {
        with(binding) {
            with(viewModel!!) {
                dataLoaded.observe(viewLifecycleOwner) {
                    if (it) initRecyclerView()
                }
                deleted.observe(viewLifecycleOwner) {
                    if (it) {
                        snackbar.apply { setText("Dues Deleted!") }.show()
                        viewModel!!.onDeleteComplete()
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        with(binding) {
            with(viewModel!!) {
                recyclerView.layoutManager =
                    GridLayoutManager(requireContext(), GridLayoutManager.VERTICAL)
                recyclerView.adapter = adapter
                adapter!!.selectedMyDues.observe(viewLifecycleOwner) { dues ->
                    if (dues != null && detailsDialogFragment == null) {
                        detailsDialogFragment = DuesDetailsDialogFragment(dues, viewModel)
                        detailsDialogFragment!!.show(
                            parentFragmentManager, DuesDetailsDialogFragment.TAG
                        )
                    }
                }

                totalPrice.observe(viewLifecycleOwner) {
                    animateTextView(lastTotal, it, tvTotalPrice)
                    lastTotal = it
                }
            }
        }
    }

    private fun animateTextView(start: Int, end: Int, textview: TextView) {
        val animator = ValueAnimator.ofInt(start, end)
        animator.duration = 1500
        animator.addUpdateListener {
            textview.text = it.animatedValue.toString()
        }
        animator.start()
    }
}