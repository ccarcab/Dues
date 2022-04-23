package es.clcarras.mydues.ui.new_due

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.databinding.NewDueFragmentBinding
import es.clcarras.mydues.ui.dialogs.date.DateDialogFragment

class NewDueFragment : Fragment() {

    private var _binding: NewDueFragmentBinding? = null
    private val binding get() = _binding!!

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

        binding.etFirstPayment.setOnClickListener { showDatePicker() }

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