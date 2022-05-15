package es.clcarras.mydues.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.databinding.PreloadDuesBottomSheetBinding
import es.clcarras.mydues.viewmodel.PreloadDuesBottomSheetViewModel

class PreloadDuesBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: PreloadDuesBottomSheetBinding
    private lateinit var viewModel: PreloadDuesBottomSheetViewModel
    private lateinit var viewModelFactory: PreloadDuesBottomSheetViewModel.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PreloadDuesBottomSheetBinding.inflate(layoutInflater)
        viewModelFactory = PreloadDuesBottomSheetViewModel.Factory(
            DuesRoomDatabase.getDatabase(requireContext())
        )
        viewModel =
            ViewModelProvider(this, viewModelFactory)[PreloadDuesBottomSheetViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.dataLoaded.observe(viewLifecycleOwner) {
            if (it) initRecyclerView()
        }
    }

    private fun initRecyclerView() {
        binding.rvPreloadDues.apply {
            adapter = viewModel.adapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
        }
        viewModel.adapter!!.selectedAppPackage.observe(viewLifecycleOwner) { pkg ->
            if (pkg.isNotBlank()) {
                openApp(pkg)
                viewModel.adapter!!.onAppOpen()
            }
        }
    }

    private fun openApp(pkg: String) {

        var intent =
            requireContext().packageManager.getLaunchIntentForPackage(pkg)

        if (intent == null)
            intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details")
                    .buildUpon()
                    .appendQueryParameter("id", pkg).build()
            }

        requireContext().startActivity(intent)
    }

}