package es.clcarras.mydues.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.constants.GRACE_PERIOD
import es.clcarras.mydues.database.WorkerDao
import es.clcarras.mydues.databinding.HomeFragmentBinding
import es.clcarras.mydues.model.Worker
import es.clcarras.mydues.viewmodel.HomeViewModel
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: HomeFragmentBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var viewModelFactory: HomeViewModel.Factory

    private lateinit var snackbar: Snackbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        viewModelFactory = HomeViewModel.Factory(
            resources.getStringArray(R.array.recurrence_array)
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        setObservers()
        initRecyclerView()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        with((requireActivity() as MainActivity)) {
            supportActionBar?.show()
            getBottomAppBar().performShow()
        }
        checkUserWorkers()
        setFabAction()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bottom_app_bar, menu)

        viewModel.launcherEnabled.observe(viewLifecycleOwner) {
            menu.findItem(R.id.launcher).isVisible = it
        }

        val onActionListener = object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                (requireActivity() as MainActivity).getFab().hide()
                binding.tvTotalPrice.visibility = View.GONE
                binding.tvCurrency.visibility = View.GONE
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                (requireActivity() as MainActivity).getFab().show()
                binding.tvTotalPrice.visibility = View.VISIBLE
                binding.tvCurrency.visibility = View.VISIBLE
                return true
            }
        }
        menu.findItem(R.id.filter).apply {
            setOnActionExpandListener(onActionListener)
            val searchView = actionView as SearchView
            searchView.queryHint = getString(R.string.filter_hint)
            searchView.setOnQueryTextListener(viewModel.onQueryTextListener)
        }

        menu.findItem(R.id.launcher).setOnMenuItemClickListener {
            findNavController().navigate(R.id.nav_preload_dues)
            true
        }

        viewModel.launcherEnabled.observe(viewLifecycleOwner) {
            menu.findItem(R.id.launcher).isVisible = it
        }

    }

    private fun setFabAction() {
        with((requireActivity() as MainActivity).getFab()) {
            setImageResource(android.R.drawable.ic_menu_add)
            setOnClickListener {
                findNavController().navigate(R.id.nav_dues_selector)
            }
            show()
            snackbar = Snackbar.make(this, "", Snackbar.LENGTH_LONG).apply {
                anchorView = this@with
            }
        }
    }

    private fun setObservers() {
        with(binding) {
            with(viewModel!!) {
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
                    animateTextView(it, tvTotalPrice)
                }
            }
        }
    }

    private fun animateTextView(target: Int, textview: TextView) {
        val animator = ValueAnimator.ofInt(0, target)
        animator.duration = 1500
        animator.addUpdateListener {
            textview.text = it.animatedValue.toString()
        }
        animator.start()
    }

    private fun checkUserWorkers() {
        WorkerDao().getMyWorkers().addOnSuccessListener { col ->
            for (doc in col) {
                val worker = doc.toObject(Worker::class.java)
                val periodicityInMillis = (worker.periodicity * 36e5).toLong()
                val delayInMillis = worker.targetDate!!.time - System.currentTimeMillis() - GRACE_PERIOD
                if (delayInMillis <= 0) {
                    worker.targetDate!!.time += periodicityInMillis
                    WorkerDao().updateWorker(worker).addOnSuccessListener {
                        (requireActivity() as MainActivity).createWorkRequest(
                            worker.message!!,
                            periodicityInMillis,
                            delayInMillis,
                            worker.uuid
                        )
                    }
                } else
                (requireActivity() as MainActivity).createWorkRequest(
                    worker.message!!,
                    periodicityInMillis,
                    delayInMillis,
                    worker.uuid
                )
            }
        }
    }
}