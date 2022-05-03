package es.clcarras.mydues.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import es.clcarras.mydues.R
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
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        with(binding) {
            with(dues) {
                etPrice.setText(price)
                etName.setText(name)

                if (description.isNullOrBlank()) tilDesc.visibility = View.GONE
                else etDesc.setText(description)

                Log.i(TAG, "$recurrence")
                with(recurrence.split("\\s".toRegex())) {
                    etEvery.setText(this[0])
                    spRecurrence.setSelection(
                        resources.getStringArray(R.array.recurrence_array).indexOf(this[1])
                    )
                    spRecurrence.isClickable = !spRecurrence.isClickable
                    spRecurrence.isEnabled = !spRecurrence.isEnabled
                }
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
        return binding.root
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

                var error = false

                // Price
                if (etPrice.length() > 0) price = etPrice.text.toString()
                else {
                    etPrice.error = "Price Required"
                    error = true
                }

                // Name
                if (etName.length() > 0) name = etName.text.toString()
                else {
                    etName.error = "Name Required"
                    error = true
                }

                // First Payment
                if (etFirstPayment.length() > 0) firstPayment = etFirstPayment.text.toString()
                else {
                    etFirstPayment.error = "First Payment Required"
                    error = true
                }

                if (error) return

                // Payment Method
                paymentMethod =
                    if (etPaymentMethod.length() > 0) etFirstPayment.text.toString() else ""

                // Description
                description =
                    if (etDesc.length() > 0) etDesc.text.toString() else ""

                // Recurrence
                recurrence =
                    if (etEvery.length() > 0) "${etEvery.text} ${spRecurrence.selectedItem}"
                    else "1 ${spRecurrence.selectedItem}"

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

            if (tilDesc.visibility == View.VISIBLE && etDesc.text.isNullOrBlank()) tilDesc.visibility = View.GONE
            else tilDesc.visibility = View.VISIBLE

            if (tilPaymentMethod.visibility == View.VISIBLE && etPaymentMethod.text.isNullOrBlank()) tilPaymentMethod.visibility = View.GONE
            else tilPaymentMethod.visibility = View.VISIBLE

            etPrice.isEnabled = !etPrice.isEnabled
            etName.isEnabled = !etName.isEnabled
            etDesc.isEnabled = !etDesc.isEnabled
            etEvery.isEnabled = !etEvery.isEnabled
            spRecurrence.isClickable = !spRecurrence.isClickable
            spRecurrence.isEnabled = !spRecurrence.isEnabled
            etFirstPayment.isEnabled = !etFirstPayment.isEnabled
            etPaymentMethod.isEnabled = !etPaymentMethod.isEnabled
        }
    }

}