package es.clcarras.mydues.ui.dialogs.dues_details

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import es.clcarras.mydues.R
import es.clcarras.mydues.Utility
import es.clcarras.mydues.databinding.DuesDetailsDialogFragmentBinding
import es.clcarras.mydues.model.Dues
import es.clcarras.mydues.ui.dialogs.DateDialogFragment
import es.clcarras.mydues.ui.home.HomeFragment

class DuesDetailsDialogFragment(
    private val fragment: HomeFragment,
    private val dues: Dues
) : DialogFragment() {

    private var _binding: DuesDetailsDialogFragmentBinding? = null
    private val binding get() = _binding!!

    private var currentColor: Int = 0
    private var contrastColor: Int = 0

    private var duesChanged = false

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

                with(recurrence.split("\\s".toRegex())) {
                    etEvery.setText(this[0])
                    spRecurrence.setSelection(
                        resources.getStringArray(R.array.recurrence_array).indexOf(this[1])
                    )
                    spRecurrence.isClickable = !spRecurrence.isClickable
                    spRecurrence.isEnabled = !spRecurrence.isEnabled
                    spRecurrence.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(
                            p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                        ) { setContrast() }
                        override fun onNothingSelected(p0: AdapterView<*>?) { }
                    }
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
                        setContrast()
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

    private fun setContrast() {
        Log.i("setContrast", "Called setContrast")
        contrastColor = Utility.contrastColor(currentColor)
        val colorStateList = ColorStateList.valueOf(contrastColor)
        with(binding) {
            container.children.forEach {
                if (it is TextInputLayout && it.editText?.currentTextColor != contrastColor) {
                    it.setSuffixTextColor(colorStateList)
                    it.defaultHintTextColor = colorStateList
                    it.boxBackgroundColor = contrastColor
                    it.boxStrokeColor = contrastColor
                    it.editText?.setTextColor(contrastColor)
                    it.editText?.backgroundTintList = colorStateList
                } else if (it is Spinner) {
                    it.backgroundTintList = colorStateList
                    (it.getChildAt(0) as? TextView)?.setTextColor(contrastColor)
                }
            }
            scrollView.setBackgroundColor(contrastColor)
        }
    }

    private fun showDatePicker() {
        DateDialogFragment.newInstance { _, year, month, day ->
            // +1 because January is zero
            val selectedDate = "$day / ${month + 1} / $year"
            binding.etFirstPayment.setText(selectedDate)
        }.show(parentFragmentManager, DateDialogFragment.TAG)
    }

    private fun saveChanges() {
        updateDues()
        if (duesChanged) {
            Log.i("saveChanges", "Cambios guardados")
            fragment.updateDues(dues)
            duesChanged = false
        }
        toggleViewEditMode()
    }

    private fun updateDues() {
        with(binding) {
            with(dues) {
                var error = false
                // Price
                if (etPrice.length() > 0) {
                    if (etPrice.text.toString() != price) {
                        price = etPrice.text.toString()
                        duesChanged = true
                    }
                } else {
                    etPrice.error = "Price Required"
                    error = true
                }
                // Name
                if (etName.length() > 0) {
                    if (etName.text.toString() != name) {
                        name = etName.text.toString()
                        duesChanged = true
                    }
                } else {
                    etName.error = "Name Required"
                    error = true
                }
                // First Payment
                if (etFirstPayment.length() > 0) {
                    if (etFirstPayment.text.toString() != firstPayment) {
                        firstPayment = etFirstPayment.text.toString()
                        duesChanged = true
                    }
                } else {
                    etFirstPayment.error = "First Payment Required"
                    error = true
                }

                if (error) return
                // Payment Method
                paymentMethod = if (etPaymentMethod.text.toString() != paymentMethod) {
                    duesChanged = true
                    etPaymentMethod.text.toString()
                } else paymentMethod

                // Description
                description = if (etDesc.text.toString() != description) {
                    duesChanged = true
                    etDesc.text.toString()
                } else description

                // Recurrence
                recurrence = if ("${etEvery.text} ${spRecurrence.selectedItem}" != recurrence) {
                    duesChanged = true
                    "${etEvery.text} ${spRecurrence.selectedItem}"
                } else "1 ${spRecurrence.selectedItem}"

                // Card Color
                cardColor = if (currentColor != cardColor) {
                    duesChanged = true
                    currentColor
                } else cardColor
            }
        }
    }

    private fun toggleViewEditMode() {
        with(binding) {
            container.children.forEach {
                if (it.visibility == View.GONE)
                    it.visibility = View.VISIBLE
                else if (it.visibility == View.VISIBLE)
                    if (it is Button || it is TextInputLayout && it.editText!!.text.isEmpty())
                        it.visibility = View.GONE

                if (it is TextInputLayout) it.editText?.isEnabled = !it.editText?.isEnabled!!
                if (it is Spinner) it.isEnabled = !it.isEnabled
            }
            setContrast()
        }
    }

}