package es.clcarras.mydues.ui

import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.DialogPriceRangeBinding
import es.clcarras.mydues.utils.Utility
import es.clcarras.mydues.viewmodel.PriceRangeDialogViewModel
import es.clcarras.mydues.viewmodel.PriceRangeDialogViewModel.Companion.END_DATE
import es.clcarras.mydues.viewmodel.PriceRangeDialogViewModel.Companion.INIT_DATE

/**
 * DialogFragment que muestra un diálogo mediante el cuál el usuario podrá calcular
 * el gasto en cuotas entre dos fechas
 */
class PriceRangeDialogFragment : DialogFragment() {

    private lateinit var binding: DialogPriceRangeBinding
    private lateinit var viewModel: PriceRangeDialogViewModel
    private lateinit var viewModelFactory: PriceRangeDialogViewModel.Factory

    /**
     * Método que crea e inicializa el cuadro de diálogo
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogPriceRangeBinding.inflate(layoutInflater)
        viewModelFactory = PriceRangeDialogViewModel.Factory(
            resources.getStringArray(R.array.recurrence_array)
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[PriceRangeDialogViewModel::class.java]

        // Se crea y devuelve el cuadro de diálogo
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    /**
     * Método que crea la vista del diálogo
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Se establece un fondo personalizado
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        return binding.root
    }

    /**
     * Método llamado cuando se ha creado la vista
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            with(binding) {

                etInitDate.setOnClickListener {
                    datePicker(INIT_DATE).show(childFragmentManager, DateDialogFragment.TAG)
                }
                etEndDate.setOnClickListener {
                    datePicker(END_DATE).show(childFragmentManager, DateDialogFragment.TAG)
                }
                initDate.observe(viewLifecycleOwner) {
                    etInitDate.setText(Utility.formatDate(it))
                }
                endDate.observe(viewLifecycleOwner) {
                    etEndDate.setText(Utility.formatDate(it))
                }
                totalPrice.observe(viewLifecycleOwner) {
                    animateTextView(it.toFloat(), tvTotalPrice)
                }
            }
        }
    }

    /**
     * Método para crear una animación en el text view recibido por parámetros
     */
    private fun animateTextView(target: Float, textview: TextView) {
        val animator = ValueAnimator.ofFloat(0f, target)
        animator.duration = 1500
        animator.addUpdateListener {
            textview.text = String.format("%.2f", it.animatedValue)
        }
        animator.start()
    }
}