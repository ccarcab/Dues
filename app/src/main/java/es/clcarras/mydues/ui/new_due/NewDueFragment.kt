package es.clcarras.mydues.ui.new_due

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.NewDueFragmentBinding
import es.clcarras.mydues.ui.dialogs.DateDialogFragment
import vadiole.colorpicker.ColorPickerDialog

class NewDueFragment : Fragment() {

    private var _binding: NewDueFragmentBinding? = null
    private val binding get() = _binding!!

    private var currentColor: Int = 0

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

        currentColor = ContextCompat.getColor(requireContext(), R.color.default_card_color)

        with(binding) {
            etFirstPayment.setOnClickListener { showDatePicker() }
            btnColorPicker.setOnClickListener { showColorPicker() }
        }

    }

    private fun showColorPicker() {
        ColorPickerDialog.Builder()
            .setInitialColor(currentColor)
            .setColorModel(vadiole.colorpicker.ColorModel.HSV)
            .setColorModelSwitchEnabled(true)
            .setButtonOkText(android.R.string.ok)
            .setButtonCancelText(android.R.string.cancel)
            .onColorSelected { color: Int ->
                currentColor = color
                with(binding.btnColorPicker) {
                    setBackgroundColor(color)
                    setTextColor(
                        if (ColorUtils.calculateLuminance(currentColor) < 0.5) Color.WHITE
                        else Color.BLACK
                    )
                }
            }
            .create()
            .show(parentFragmentManager, "color_picker")
    }

    private fun showDatePicker() {
        val newFragment = DateDialogFragment.newInstance { _, year, month, day ->
            // +1 because January is zero
            val selectedDate = day.toString() + " / " + (month + 1) + " / " + year
            binding.etFirstPayment.setText(selectedDate)
        }

        newFragment.show(parentFragmentManager, DateDialogFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}