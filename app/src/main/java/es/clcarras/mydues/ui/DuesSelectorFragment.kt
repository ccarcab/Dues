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
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.FragmentDuesSelectorBinding
import es.clcarras.mydues.viewmodel.DuesSelectorViewModel

/**
 * Fragmento que muestra el listado de cuotas precargadas
 */
class DuesSelectorFragment : Fragment() {

    private lateinit var binding: FragmentDuesSelectorBinding
    private lateinit var viewModel: DuesSelectorViewModel

    /**
     * Método que inicializa la vista
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDuesSelectorBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[DuesSelectorViewModel::class.java]

        // Se navega sin pasar argumentos
        binding.btnNewDues.setOnClickListener {
            findNavController().navigate(R.id.nav_new_due)
        }

        // Se inicializa el listado de cuotas
        with(binding.rvDuesSelector) {
            adapter = viewModel.adapter
            layoutManager = GridLayoutManager(requireContext(), GridLayoutManager.VERTICAL)
        }

        return binding.root
    }

    /**
     * Método llamado al crear el Fragment
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Se establece que tendrá options menu
        setHasOptionsMenu(true)
    }

    /**
     * Método llamado cuando se restaura la vista, por ejemplo al hacer popback
     */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        (requireActivity() as MainActivity).getFab().hide()
    }

    /**
     * Método que crea el menú de opciones
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Se infla el menú
        inflater.inflate(R.menu.bottom_app_bar, menu)

        // Evento de escucha cuando se expande la barra de búsqueda
        val onActionListener = object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                // Se pega el listado a la parte inferior del layout
                binding.rvDuesSelector
                    .updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 1.0f }
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                // Se pega el listado a la parte superior del layout
                binding.rvDuesSelector
                    .updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 0.0f }
                return true
            }
        }
        // Se inicializa el filtro
        menu.findItem(R.id.filter).apply {
            setOnActionExpandListener(onActionListener)
            val searchView = actionView as SearchView
            searchView.queryHint = getString(R.string.filter_hint)
            searchView.setOnQueryTextListener(viewModel.onQueryTextListener)
        }

        // Se ocultan los elementos de launcher y menú
        menu.findItem(R.id.launcher).isVisible = false
        menu.findItem(R.id.menu).isVisible = false
    }

}