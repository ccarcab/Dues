package es.clcarras.mydues.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.BottomSheetMenuBinding

/**
 * BottomSheetDialogFragment que muestra un cuadro de diálogo con opciones adicionales
 */
class MenuBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetMenuBinding

    /**
     * Método que crea la vista del diálogo
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetMenuBinding.inflate(inflater, container, false)
        with(binding) {
            // Onclick para el botón de cierre de sesión
            btnLogout.setOnClickListener { findNavController().navigate(R.id.nav_logout) }
            // Onclick para el botón del diálogo de cálculo de gasto
            btnPriceRange.setOnClickListener { findNavController().navigate(R.id.nav_price_range) }
        }
        return binding.root
    }

}