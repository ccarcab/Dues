package es.clcarras.mydues.ui.new_due

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.Utility
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.databinding.NewDueFragmentBinding
import es.clcarras.mydues.model.Dues
import es.clcarras.mydues.ui.dialogs.DateDialogFragment
import kotlinx.coroutines.launch
import vadiole.colorpicker.ColorPickerDialog

class NewDueFragment : Fragment() {

    private var _binding: NewDueFragmentBinding? = null
    private val binding get() = _binding!!

    private var selectedColor: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewDueFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).getFab()?.hide()

        selectedColor = ContextCompat.getColor(requireContext(), R.color.default_card_color)
        setColorToPicker(selectedColor)
        binding.btnColorPicker

        setOnClickListeners()

    }

    private fun setColorToPicker(color: Int) {
        with(binding.btnColorPicker) {
            setBackgroundColor(color)
            setTextColor(Utility.contrastColor(color))
        }
    }

    private fun setOnClickListeners() {
        with(binding) {
            etPrice.setOnClickListener { it as EditText
                it.error = null
            }
            etName.setOnClickListener { it as EditText
                etName.error = null
            }
            etFirstPayment.setOnClickListener { it as EditText
                showDatePicker()
                it.error = null
            }
            btnColorPicker.setOnClickListener { showColorPicker() }
            btnSave.setOnClickListener { saveDues() }
        }
    }

    private fun showDatePicker() {
        DateDialogFragment.newInstance { _, year, month, day ->
            // +1 because January is zero
            val selectedDate = "$day / ${month + 1} / $year"
            binding.etFirstPayment.setText(selectedDate)
        }.show(parentFragmentManager, DateDialogFragment.TAG)
    }

    private fun showColorPicker() {
        ColorPickerDialog.Builder()
            .setInitialColor(selectedColor)
            .setColorModel(vadiole.colorpicker.ColorModel.HSV)
            .setColorModelSwitchEnabled(true)
            .setButtonOkText(android.R.string.ok)
            .setButtonCancelText(android.R.string.cancel)
            .onColorSelected { color: Int ->
                selectedColor = color
                setColorToPicker(color)
            }
            .create()
            .show(parentFragmentManager, "color_picker")
    }

    private fun saveDues() {
        with(binding) {

            var price = ""
            var name = ""
            var firstPayment = ""

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
            val paymentMethod: String =
                if (etPaymentMethod.length() > 0) etFirstPayment.text.toString() else ""

            // Description
            val description: String =
                if (etDesc.length() > 0) etDesc.text.toString() else ""

            // Recurrence
            val recurrence: String =
                if (etEvery.length() > 0) "${etEvery.text} ${spRecurrence.selectedItem}"
                else "1 ${spRecurrence.selectedItem}"

            val db = DuesRoomDatabase.getDatabase(requireContext())
            lifecycleScope.launch {
                db.duesDao().insert(
                    Dues(
                        price = price,
                        name = name,
                        description = description,
                        recurrence = recurrence,
                        firstPayment = firstPayment,
                        paymentMethod = paymentMethod,
                        cardColor = selectedColor
                    )
                )
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}