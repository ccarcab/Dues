package es.clcarras.mydues.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.BottomSheetMenuBinding

class MenuBottomSheet: BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetMenuBinding.inflate(inflater, container, false)
        binding.btnLogout.setOnClickListener {
            findNavController().navigate(R.id.nav_logout)
        }
        return binding.root
    }

}