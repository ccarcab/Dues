package es.clcarras.mydues.ui.new_due

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.Utility
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.databinding.NewDuesFragmentBinding
import es.clcarras.mydues.ui.dialogs.DateDialogFragment

class NewDueFragment : Fragment() {

    private var _binding: NewDuesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NewDueViewModel
    private lateinit var viewModelFactory: NewDueViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewDuesFragmentBinding.inflate(inflater, container, false)
        viewModelFactory = NewDueViewModelFactory(
            DuesRoomDatabase.getDatabase(requireContext()),
            getColor(requireContext(), R.color.default_card_color),
            getColor(requireContext(), R.color.black)
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[NewDueViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        (requireActivity() as MainActivity).getFab()?.hide()

        setOnTextChanged()
        setOnClickListeners()
        setObservers()
        setSpinner()

        return binding.root
    }

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

    private fun setObservers() {
        with(binding) {
            with(viewModel!!) {
                contrastColor.observe(viewLifecycleOwner) {
                    btnColorPicker.setTextColor(it)
                }
                cardColor.observe(viewLifecycleOwner) {
                    btnColorPicker.setBackgroundColor(it)
                }
                error.observe(viewLifecycleOwner) {
                    if (it.isNotBlank()) {
                        Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        etPrice.error = ""
                        etName.error = ""
                        etFirstPayment.error = ""
                    }
                }
                insert.observe(viewLifecycleOwner) {
                    if (it) {
                        Snackbar.make(requireView(), "Dues Created!", Snackbar.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

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