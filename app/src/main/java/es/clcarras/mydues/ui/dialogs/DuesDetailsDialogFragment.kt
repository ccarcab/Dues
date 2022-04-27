package es.clcarras.mydues.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import es.clcarras.mydues.Utility
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.databinding.DuesDetailsDialogFragmentBinding
import es.clcarras.mydues.model.Dues
import es.clcarras.mydues.ui.home.HomeFragment

class DuesDetailsDialogFragment(
    private val fragment: HomeFragment,
    private val dues: Dues
) : DialogFragment() {

    private var _binding: DuesDetailsDialogFragmentBinding? = null
    private val binding get() = _binding!!

    private var currentColor: Int = 0

    companion object {
        const val TAG = "DuesDetailsDialogFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = DuesDetailsDialogFragmentBinding.inflate(layoutInflater)
        with(binding) {
            with(dues) {
                etPrice.setText(price)
                etName.setText(name)

                if (description.isNullOrBlank()) tilDesc.visibility = View.GONE
                else etDesc.setText(description)

                etEvery.setText(recurrence)
                etFirstPayment.setText(firstPayment)

                if (paymentMethod.isNullOrBlank()) tilPaymentMethod.visibility = View.GONE
                else etPaymentMethod.setText(paymentMethod)

                currentColor = cardColor
                container.setBackgroundColor(cardColor)
            }

            etFirstPayment.setOnClickListener { showDatePicker() }

            btnColorPicker.setOnClickListener {
                Utility.colorPicker(currentColor)
                    .onColorSelected { color: Int ->
                        currentColor = color
                        container.setBackgroundColor(color)
                    }
                    .create()
                    .show(childFragmentManager, Utility.TAG)
            }
            btnClose.setOnClickListener { dialog?.dismiss() }
            btnEdit.setOnClickListener { toggleViewEditMode() }
            btnSave.setOnClickListener { saveChanges() }
            btnDelete.setOnClickListener {
                fragment.deleteDues(dues)
                dialog?.dismiss()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun showDatePicker() {
        DateDialogFragment.newInstance { _, year, month, day ->
            // +1 because January is zero
            val selectedDate = "$day / ${month + 1} / $year"
            binding.etFirstPayment.setText(selectedDate)
        }.show(parentFragmentManager, DateDialogFragment.TAG)
    }

    private fun saveChanges() {
        with(binding) {
            toggleViewEditMode()
            with(dues) {
                price = etPrice.text.toString()
                name = etName.text.toString()

                if (etDesc.visibility == View.VISIBLE)
                    description = "${etDesc.text}"

                recurrence = etEvery.text.toString()
                firstPayment = etFirstPayment.text.toString()

                if (etPaymentMethod.visibility == View.VISIBLE)
                    paymentMethod = "${etPaymentMethod.text}"
                cardColor = currentColor
            }

            fragment.updateDues(dues)
        }
    }

    private fun toggleViewEditMode() {
        with(binding) {
            if (btnDelete.visibility == View.GONE) btnDelete.visibility = View.VISIBLE
            else btnDelete.visibility = View.GONE

            if (btnEdit.visibility == View.GONE) btnEdit.visibility = View.VISIBLE
            else btnEdit.visibility = View.GONE

            if (btnSave.visibility == View.VISIBLE) btnSave.visibility = View.GONE
            else btnSave.visibility = View.VISIBLE

            if (btnColorPicker.visibility == View.VISIBLE) btnColorPicker.visibility = View.GONE
            else btnColorPicker.visibility = View.VISIBLE

            etPrice.isEnabled = !etPrice.isEnabled
            etName.isEnabled = !etName.isEnabled
            etDesc.isEnabled = !etDesc.isEnabled
            etEvery.isEnabled = !etEvery.isEnabled
            etFirstPayment.isEnabled = !etFirstPayment.isEnabled
            etPaymentMethod.isEnabled = !etPaymentMethod.isEnabled
        }
    }

}