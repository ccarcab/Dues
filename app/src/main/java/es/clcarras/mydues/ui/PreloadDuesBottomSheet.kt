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

/**
 * BottomSheetDialogFragment que muestra el listado de iconos de
 * aplicaciones disponibles para lanzar
 */
class PreloadDuesBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetPreloadDuesBinding
    private lateinit var viewModel: PreloadDuesBottomSheetViewModel

    /**
     * Método que crea la vista
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetPreloadDuesBinding.inflate(layoutInflater)
        viewModel =
            ViewModelProvider(this)[PreloadDuesBottomSheetViewModel::class.java]

        initRecyclerView()

        // Si se selecciona un icono se abre la aplicación asociada
        viewModel.adapter!!.selectedAppPackage.observe(viewLifecycleOwner) { pkg ->
            if (pkg.isNotBlank()) {
                openApp(pkg)
                viewModel.adapter!!.onAppOpen()
            }
        }
        return binding.root
    }

    /**
     * Método que inicializa el listado de iconos
     */
    private fun initRecyclerView() {
        binding.rvPreloadDues.apply {
            adapter = viewModel.adapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
        }
    }

    /**
     * Método que abre la aplicación asociada a cuota mediante un intent
     */
    private fun openApp(pkg: String) {
        // Intent para abrir la aplicación instalada en el dispositivo
        var intent =
            requireContext().packageManager.getLaunchIntentForPackage(pkg)

        // Si la app no está instalada en el dispositivo se obtiene un intent para abrir
        // la página de la misma aplicación en la play store
        if (intent == null)
            intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(URI_PLAY_STORE)
                    .buildUpon()
                    .appendQueryParameter("id", pkg).build()
            }

        // Se lanza el intent
        requireContext().startActivity(intent)
    }

}