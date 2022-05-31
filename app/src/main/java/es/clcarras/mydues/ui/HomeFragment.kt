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
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.constants.GRACE_PERIOD
import es.clcarras.mydues.database.WorkerDao
import es.clcarras.mydues.databinding.FragmentHomeBinding
import es.clcarras.mydues.model.Worker
import es.clcarras.mydues.viewmodel.HomeViewModel

/**
 * Fragment que muestra un listado de las cuotas del usuario
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var viewModelFactory: HomeViewModel.Factory

    private lateinit var snackbar: Snackbar

    /**
     * Método que inicializa la vista
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
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
        with((requireActivity() as MainActivity)) {
            supportActionBar?.show()
            getBottomAppBar().performShow()
        }
        // Se comprueban los workers del usuario
        checkUserWorkers()
        // Se define la acción que realizará el fab en esta vista
        setFabAction()
    }

    /**
     * Método que crea el menú de opciones
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Se infla el menú
        inflater.inflate(R.menu.bottom_app_bar, menu)

        // El botón launcher será visible cuando pueda estar habilitado
        viewModel.launcherEnabled.observe(viewLifecycleOwner) {
            menu.findItem(R.id.launcher).isVisible = it
        }

        // Evento de escucha cuando se expande la barra de búsqueda
        val onActionListener = object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                // Se ocultan los elementos innecesarios
                (requireActivity() as MainActivity).getFab().hide()
                binding.tvTotalPrice.visibility = View.GONE
                binding.tvCurrency.visibility = View.GONE
                binding.tvRecurrence.visibility = View.GONE
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                // Se vuelven a mostrar los elementos ocultos
                (requireActivity() as MainActivity).getFab().show()
                binding.tvTotalPrice.visibility = View.VISIBLE
                binding.tvCurrency.visibility = View.VISIBLE
                binding.tvRecurrence.visibility = View.VISIBLE
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

        // Se añade el onclick al botón launcher
        menu.findItem(R.id.launcher).setOnMenuItemClickListener {
            findNavController().navigate(R.id.nav_preload_dues)
            true
        }

        // Se añade el onclick al botón menú
        menu.findItem(R.id.menu).setOnMenuItemClickListener {
            findNavController().navigate(R.id.nav_menu)
            true
        }

    }

    /**
     * Método que asigna la función que hará el fab en esta vista
     */
    private fun setFabAction() {
        with((requireActivity() as MainActivity).getFab()) {
            // Se le cambia el icono
            setImageResource(android.R.drawable.ic_menu_add)
            // se establece su onclik
            setOnClickListener { findNavController().navigate(R.id.nav_dues_selector) }
            // Se muestra
            show()
            snackbar = Snackbar.make(this, "", Snackbar.LENGTH_LONG).apply {
                anchorView = this@with
            }
        }
    }

    /**
     * Método que establece los eventos de escucha a cambios en los LiveData del ViewModel
     */
    private fun setObservers() {
        with(binding) {
            with(viewModel!!) {
                // Si se ha borrado la cuota seleccionada se muestra un snackbar
                deleted.observe(viewLifecycleOwner) {
                    if (it) {
                        snackbar.apply { setText("Dues Deleted!") }.show()
                        viewModel!!.onDeleteComplete()
                    }
                }
                // Si se ha seleccionado una cuota del listado, se muestran los detalles
                adapter!!.selectedMyDues.observe(viewLifecycleOwner) { dues ->
                    if (dues != null && detailsDialogFragment == null) {
                        detailsDialogFragment = DuesDetailsDialogFragment(dues, viewModel)
                        detailsDialogFragment!!.show(
                            parentFragmentManager, DuesDetailsDialogFragment.TAG
                        )
                    }
                }
                // Si cambia el precio total se anima el texto mostrado
                totalPrice.observe(viewLifecycleOwner) {
                    animateTextView(it.toFloat(), tvTotalPrice)
                }
                noDues.observe(viewLifecycleOwner) { noDues ->
                    ivBackground.visibility = if (noDues) View.VISIBLE else View.GONE
                }
            }
        }
    }

    /**
     * Método que inicializa el listado de cuotas
     */
    private fun initRecyclerView() {
        with(binding) {
            with(viewModel!!) {
                recyclerView.layoutManager =
                    GridLayoutManager(requireContext(), GridLayoutManager.VERTICAL)
                recyclerView.adapter = adapter
            }
        }
    }

    /**
     * Método que crea una animación en el campo de texto que recibe por parámetros
     */
    private fun animateTextView(target: Float, textview: TextView) {
        val animator = ValueAnimator.ofFloat(0f, target)
        animator.duration = 1500
        animator.addUpdateListener {
            textview.text = String.format("%.2f", it.animatedValue)
        }
        animator.start()
    }

    /**
     * Método que comprueba los workers del usuario
     */
    private fun checkUserWorkers() {
        // Se obtienen todos los workers del usuario
        WorkerDao().getMyWorkers().addOnSuccessListener { col ->
            for (doc in col) { // Por cada documento de worker
                // Se almacenan los datos del documento en un data class Worker
                val worker = doc.toObject(Worker::class.java)
                // Se obtiene la periodicidad
                val periodicityInMillis = (worker.periodicity * 36e5).toLong()
                // Se calcula el delay
                val delayInMillis =
                    worker.targetDate!!.time - System.currentTimeMillis() - GRACE_PERIOD
                // Si el delay es menor o igual a 0
                if (delayInMillis <= 0) {
                    // Se añade otro periodo al worker y se actualiza
                    worker.targetDate!!.time += periodicityInMillis
                    WorkerDao().updateWorker(worker)
                }
                // Se despierta o crea al worker
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