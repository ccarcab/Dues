package es.clcarras.mydues.ui

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.FragmentNewDuesBinding
import es.clcarras.mydues.utils.Utility
import es.clcarras.mydues.viewmodel.NewDuesViewModel

/**
 * Fragment que muestra una ventana mediante la cuál el usuario puede crear una cuota
 */
class NewDuesFragment : Fragment() {

    private lateinit var binding: FragmentNewDuesBinding
    private lateinit var viewModel: NewDuesViewModel
    private lateinit var viewModelFactory: NewDuesViewModel.Factory

    private lateinit var snackbar: Snackbar

    /**
     * Método que crea la vista
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewDuesBinding.inflate(inflater, container, false)
        viewModelFactory = NewDuesViewModel.Factory(
            NewDuesFragmentArgs.fromBundle(requireArguments()),
            getColor(requireContext(), R.color.white),
            resources.getStringArray(R.array.recurrence_array)
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[NewDuesViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setOnTextChanged()
        setOnClickListeners()
        setSpinner()
        setObservers()

        viewModel.checkSelectedDues()

        return binding.root
    }

    /**
     * Método llamado cuando se restaura la vista
     */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // Se establece la acción que realizará el fab en esta vista
        setFabAction()
        // Se oculta la barra de navegación inferior
        (requireActivity() as MainActivity).getBottomAppBar().performHide(true)
    }

    /**
     * Método que establece la acción que realizará el fab en esta vista
     */
    private fun setFabAction() {
        with((requireActivity() as MainActivity).getFab()) {
            setImageResource(android.R.drawable.ic_menu_save) // Se cambia la imagen
            setOnClickListener { viewModel.checkData() } // Al pulsar el botón se guardará la cuota
            show()
            // Se inicializa el snackbar anclándolo al fab
            snackbar = Snackbar.make(this, "", Snackbar.LENGTH_LONG).apply {
                anchorView = this@with
            }
        }
    }

    /**
     * Método que establece los eventos de escucha de cambio en el texto en
     * los campos de introducción de datos
     */
    private fun setOnTextChanged() {
        with(binding) {
            with(viewModel!!) {
                etPrice.doOnTextChanged { text, _, _, _ -> setPrice(text.toString()) }
                etName.doOnTextChanged { text, _, _, _ -> setName(text.toString()) }
                etDesc.doOnTextChanged { text, _, _, _ -> setDesc(text.toString()) }
                etEvery.doOnTextChanged { text, _, _, _ -> setEvery(text.toString()) }
                etPaymentMethod.doOnTextChanged { text, _, _, _ -> setPaymentMethod(text.toString()) }
            }
        }
    }

    /**
     * Método que establece la escucha de clicks a los elementos de la vista
     */
    private fun setOnClickListeners() {
        with(binding) {
            with(viewModel!!) {
                etFirstPayment.setOnClickListener {
                    datePicker().show(parentFragmentManager, DateDialogFragment.TAG)
                }
                btnColorPicker.setOnClickListener {
                    colorPicker().show(childFragmentManager, Utility.TAG)
                }
            }
        }

    }

    /**
     * Método que establece la escucha de cambios de los datos almacenados en el ViewModel
     */
    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun setObservers() {
        with(binding) { // With para usar los elementos del binding
            with(viewModel!!) { // With para usar los datos del ViewModel

                // Si la cuota es precargada
                preloadDues.observe(viewLifecycleOwner) {
                    // El campo de nombre estará habilitado si no es una cuota precargada
                    etName.isEnabled = it == null
                    if (it != null) { // Si la cuota es precargada
                        with(binding) {
                            // Se muestra su icono
                            ivPreloadDues.visibility = View.VISIBLE
                            Picasso.get().load(Uri.parse(it.image)).into(ivPreloadDues)
                            ivPreloadDues.setColorFilter(Utility.contrastColor(it.color))
                            // Se establece el nombre
                            etName.setText(it.name)
                        }
                    }
                }

                // Si cambia el color elegido se cambio el color de los elementos de la vista
                cardColor.observe(viewLifecycleOwner) {
                    btnColorPicker.setBackgroundColor(it)
                    btnColorPicker.setTextColor(Utility.contrastColor(it))
                    btnColorPicker.compoundDrawableTintList =
                        ColorStateList.valueOf(Utility.contrastColor(it))
                    etPrice.backgroundTintList = ColorStateList.valueOf(it)
                    etPrice.setTextColor(Utility.contrastColor(it))
                    etPrice.setHintTextColor(Utility.contrastColor(it))
                    tvCurrency.backgroundTintList = ColorStateList.valueOf(it)
                    tvCurrency.setTextColor(Utility.contrastColor(it))
                    if (ivPreloadDues.visibility == View.VISIBLE)
                        ivPreloadDues.setColorFilter(Utility.contrastColor(it))
                }

                // Si hay un error
                error.observe(viewLifecycleOwner) {
                    if (it.isNotBlank()) {
                        // Se muestra un snackbar indicando el error
                        snackbar.apply { setText(it) }.show()
                        // Se muestra un error en los campos requeridos si están vacíos
                        if (etPrice.text.isNullOrBlank())
                            etPrice.setError("Required", errorIcon())
                        if (etName.text.isNullOrBlank())
                            etName.error = "Required"
                        if (etFirstPayment.text.isNullOrBlank())
                            etFirstPayment.error = "Required"
                        if (etEvery.text.isNullOrBlank())
                            etEvery.error = "Required"
                    }
                }

                // Si cambia la fecha de primer pago
                firstPayment.observe(viewLifecycleOwner) {
                    if (it != null) {
                        etFirstPayment.error = null
                        etFirstPayment.setText(Utility.formatDate(it))
                    }
                }

                // Si la entrada de datos es válida
                validInput.observe(viewLifecycleOwner) {
                    if (it) {
                        // Se calcula el periodo de cobro en milisegundos
                        val periodTime = (periodicityInHours() * 36e5).toLong()
                        // Se obtiene el mensaje que mostrará el worker
                        val msg = getString(
                            R.string.notification_msg,
                            name.value, price.value.toString()
                        )
                        // Se crea el worker y se obtiene su uuid
                        val uuid = (requireActivity() as MainActivity).createWorkRequest(
                            msg,
                            periodTime,
                            millisUntilNextPayment()
                        ).toString()
                        saveDues(uuid, msg)
                    }
                }

                // Si se han isertado los datos, se navega a la vista home
                insert.observe(viewLifecycleOwner) {
                    if (it) {
                        snackbar.apply { setText("Dues Created!") }.show()
                        findNavController().navigate(R.id.nav_home)
                    }
                }
            }
        }
    }

    private fun errorIcon() =
        getDrawable(requireContext(), com.google.android.material.R.drawable.mtrl_ic_error)!!.apply {
            setTint(Utility.contrastErrorColor(viewModel.cardColor.value!!))
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        }

    /**
     * Método que inicializa el elemento Spinner que contiene las recurrencias posibles
     */
    private fun setSpinner() {
        with(binding) {
            with(viewModel!!) {
                spRecurrence.onItemSelectedListener = spinnerListener
                spRecurrence.setSelection(
                    resources.getStringArray(R.array.recurrence_array)
                        .indexOf(recurrence.value)
                )
            }
        }
    }

}