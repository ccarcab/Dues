package es.clcarras.mydues.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import es.clcarras.mydues.databinding.DuesDetailsDialogFragmentBinding

class DuesDetailsDialogFragment : DialogFragment() {

    private var _binding: DuesDetailsDialogFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = DuesDetailsDialogFragmentBinding.inflate(layoutInflater)

        with(DuesDetailsDialogFragmentArgs.fromBundle(requireArguments())) {
            with(binding) {
                etPrice.setText(price)

                etName.setText(name)

                if (desc.isNullOrBlank()) tilDesc.visibility = View.GONE else etDesc.setText(desc)

                etEvery.setText(recurrence)

                etFirstPayment.setText(firstPayment)

                if (paymentMethod.isNullOrBlank()) tilPaymentMethod.visibility = View.GONE
                else etPaymentMethod.setText(paymentMethod)

                container.setBackgroundColor(cardColor)
            }

        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

}