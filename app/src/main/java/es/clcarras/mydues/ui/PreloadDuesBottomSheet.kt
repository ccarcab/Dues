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
import es.clcarras.mydues.constants.URI_PLAY_STORE
import es.clcarras.mydues.databinding.BottomSheetPreloadDuesBinding
import es.clcarras.mydues.viewmodel.PreloadDuesBottomSheetViewModel

class PreloadDuesBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetPreloadDuesBinding
    private lateinit var viewModel: PreloadDuesBottomSheetViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetPreloadDuesBinding.inflate(layoutInflater)
        viewModel =
            ViewModelProvider(this)[PreloadDuesBottomSheetViewModel::class.java]

        initRecyclerView()
        return binding.root
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
                data = Uri.parse(URI_PLAY_STORE)
                    .buildUpon()
                    .appendQueryParameter("id", pkg).build()
            }

        requireContext().startActivity(intent)
    }

}