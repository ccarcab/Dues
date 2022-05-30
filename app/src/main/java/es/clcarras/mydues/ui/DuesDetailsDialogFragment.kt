package es.clcarras.mydues.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.DialogDuesDetailsBinding
import es.clcarras.mydues.model.MyDues
import es.clcarras.mydues.utils.Utility
import es.clcarras.mydues.viewmodel.DuesDetailsDialogViewModel
import es.clcarras.mydues.viewmodel.HomeViewModel

/**
 * Fragmento usado para mostrar un cuadro de diálogo de los detalles de la cuota
 * seleccionada en la vista Home
 */
class DuesDetailsDialogFragment(
    private val myDues: MyDues?, // Cuota seleccionada
    private val homeViewModel: HomeViewModel? // ViewModel de la vista Home para comunicarse con el
) : DialogFragment() {

    // Constructor sin parámetros
    constructor() : this(null, null)

    private lateinit var binding: DialogDuesDetailsBinding
    private lateinit var viewModel: DuesDetailsDialogViewModel
    private lateinit var viewModelFactory: DuesDetailsDialogViewModel.Factory

    companion object {
        // Tag usado para identificar al diálogo
        const val TAG = "DuesDetailsDialogFragment"
    }

    /**
     * Método que crea e inicializa el cuadro de diálogo
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogDuesDetailsBinding.inflate(layoutInflater)
        viewModelFactory = DuesDetailsDialogViewModel.Factory(
            myDues, homeViewModel,
            resources.getStringArray(R.array.recurrence_array)
        )
        viewModel =
            ViewModelProvider(this, viewModelFactory)[DuesDetailsDialogViewModel::class.java]
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    /**
     * Método que inicializa la vista del cuadro de diálogo
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setOnTextChanged()
        setOnClickListeners()
        setSpinner()
        setObservers()

        viewModel.checkSelectedDues()

        // Se ocultan los campos que estén vacíos
        with(binding) {
            with(myDues) {
                if (this?.description?.isBlank() == true) tilDesc.visibility = View.GONE
                if (this?.paymentMethod?.isBlank() == true) tilPaymentMethod.visibility = View.GONE
            }
        }

        return binding.root
    }

    /**
     * Método llamado cuando se cierra el diálogo
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        viewModel.close()
    }

    /**
     * Método que crea los eventos de escucha que se ejecutan cuando se escribe en los
     * campos del cuadro de diálogo
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
     * Método que establece los eventos de respuesta a clicks a los elementos de la vista
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
            btnClose.setOnClickListener {
                viewModel!!.close()
                dialog?.dismiss()
            }
            btnEdit.setOnClickListener { toggleEditMode() }
        }

    }

    /**
     * Método que establece los eventos de escucha a cambios en los LiveData del ViewModel
     */
    private fun setObservers() {
        with(binding) {
            with(viewModel!!) {
                // Si es una cuota precargada se carga y muestra su icono
                preloadDues.observe(viewLifecycleOwner) {
                    if (it != null) {
                        ivPreloadDues.visibility = View.VISIBLE
                        Picasso.get().load(Uri.parse(it.image)).into(ivPreloadDues)
                        ivPreloadDues.setColorFilter(Utility.contrastColor(it.color))
                    }
                }
                // Si se cambia su color se establece a los elementos
                cardColor.observe(viewLifecycleOwner) {
                    setColor(it)
                }
                // Si hay un error se muestra
                error.observe(viewLifecycleOwner) {
                    if (it.isNotBlank()) {
                        Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        if (etPrice.text.isNullOrBlank()) etPrice.error = "Required"
                        if (etName.text.isNullOrBlank()) etName.error = "Required"
                        if (etFirstPayment.text.isNullOrBlank()) etFirstPayment.error = "Required"
                    }
                }
                // Si se cambia la fecha se actualiza el campo de texto
                firstPayment.observe(viewLifecycleOwner) {
                    if (it != null) {
                        etFirstPayment.error = null
                        etFirstPayment.setText(Utility.formatDate(it))
                        if (it != myDues?.firstPayment) {
                            updateNotification()
                        }
                    }
                }
                // Si se cambia cada cuánto se paga la cuota se actualiza la notificación
                every.observe(viewLifecycleOwner) {
                    if (!it.isNullOrBlank() && it.toInt() != myDues?.every && it.toInt() > 0)
                        updateNotification()
                }
                recurrence.observe(viewLifecycleOwner) {
                    if (it != myDues?.recurrence && it.isNotBlank())
                        updateNotification()
                }

                // Si hay una actualización se cambia a la vista normal
                update.observe(viewLifecycleOwner) {
                    if (it) toggleEditMode()
                }

                // Si se elimina la cuota se cierra el dialog y se borra el worker
                delete.observe(viewLifecycleOwner) {
                    if (it) {
                        (requireActivity() as MainActivity).deleteWork(notificationUUID)
                        dialog?.dismiss()
                    }
                }

            }
        }
    }

    /**
     * Método que cambia el color de los elementos de la vista en función del color elegido
     */
    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun setColor(color: Int) {
        with(binding) {
            btnColorPicker.setBackgroundColor(color)
            btnColorPicker.setTextColor(Utility.contrastColor(color))
            btnColorPicker.compoundDrawableTintList =
                ColorStateList.valueOf(Utility.contrastColor(color))

            btnClose.setBackgroundColor(color)
            btnClose.setTextColor(Utility.contrastColor(color))

            btnEdit.setBackgroundColor(color)
            btnEdit.setTextColor(Utility.contrastColor(color))

            btnSave.setBackgroundColor(color)
            btnSave.setTextColor(Utility.contrastColor(color))

            btnDelete.setBackgroundColor(color)
            btnDelete.setTextColor(Utility.contrastColor(color))

            etPrice.backgroundTintList = ColorStateList.valueOf(color)
            etPrice.setTextColor(Utility.contrastColor(color))
            etPrice.setHintTextColor(Utility.contrastColor(color))

            tvCurrency.backgroundTintList = ColorStateList.valueOf(color)
            tvCurrency.setTextColor(Utility.contrastColor(color))
            if (ivPreloadDues.visibility == View.VISIBLE)
                ivPreloadDues.setColorFilter(Utility.contrastColor(color))
        }
    }

    /**
     * Método que actualiza el worker de la notificación en segundo plano
     */
    private fun updateNotification() {
        with(requireActivity() as MainActivity) {
            with(viewModel) {
                // Se obtiene cada cuando se ejecutará el worker en milisegundos
                val periodTime = (periodicityInHours() * 36e5).toLong()
                // Se crea el mensaje que mostrará la notificación
                val msg = getString(
                    R.string.notification_msg,
                    name.value, price.value.toString()
                )
                // Se crea un nuevo worker y se obtiene su uuid
                val uuid = (requireActivity() as MainActivity).createWorkRequest(
                    msg,
                    periodTime,
                    millisUntilNextPayment()
                ).toString()
                // Se elimina el worker anterior
                deleteWork(setNotification(uuid, msg))
            }
        }
    }

    /**
     * Método que inicializa el spinner de recurrencia
     */
    private fun setSpinner() {
        with(binding) {
            with(viewModel!!) {
                spRecurrence.onItemSelectedListener = spinnerListener
                spRecurrence.setSelection(
                    resources.getStringArray(R.array.recurrence_array)
                        .indexOf(recurrence.value)
                )
                spRecurrence.isEnabled = !spRecurrence.isEnabled
            }
        }
    }

    /**
     * Método que cambia de la vista de visualización a la vista de edición
     */
    private fun toggleEditMode() {
        with(binding) {
            // Se recorren los hijos de la vista contenedora y se cambia su visibilidad y su
            // estado enabled en función del tipo de campo que sean y su contenido
            container.children.forEach {
                if (it.visibility == View.GONE && it.id != binding.ivPreloadDues.id)
                    it.visibility = View.VISIBLE
                else if (it.visibility == View.VISIBLE)
                    if (it is Button || it is TextInputLayout && it.editText!!.text.isEmpty())
                        it.visibility = View.GONE

                if (it is TextInputLayout)
                    if (it.editText?.id != R.id.etName || viewModel!!.`package` != null)
                        it.editText?.isEnabled = !it.editText?.isEnabled!!

                if (it is EditText) it.isEnabled = !it.isEnabled
                if (it is Spinner) it.isEnabled = !it.isEnabled
            }
        }
    }

}