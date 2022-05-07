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
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.Utility
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.databinding.NewDuesFragmentBinding
import es.clcarras.mydues.ui.dialogs.DateDialogFragment

class NewDuesFragment : Fragment() {

    private var _binding: NewDuesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NewDuesViewModel
    private lateinit var viewModelFactory: NewDuesViewModelFactory

    private lateinit var snackbar: Snackbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewDuesFragmentBinding.inflate(inflater, container, false)
        viewModelFactory = NewDuesViewModelFactory(
            DuesRoomDatabase.getDatabase(requireContext()),
            getColor(requireContext(), R.color.default_card_color),
            getColor(requireContext(), R.color.black)
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[NewDuesViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setOnTextChanged()
        setOnClickListeners()
        setObservers()
        setSpinner()

        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        with(requireActivity().findViewById<FloatingActionButton>(R.id.fab)) {
            setImageResource(android.R.drawable.ic_menu_save)
            setOnClickListener { viewModel.saveDues() }
            snackbar = Snackbar.make(this, "", Snackbar.LENGTH_LONG).apply {
                anchorView = this@with
            }
        }
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
                        snackbar.apply { setText(it) }.show()
                        if (etPrice.text.isNullOrBlank()) etPrice.error = "Required"
                        if (etName.text.isNullOrBlank()) etName.error = "Required"
                        if (etFirstPayment.text.isNullOrBlank()) etFirstPayment.error = "Required"
                    }
                }
                firstPayment.observe(viewLifecycleOwner) {
                    if (it.isNotBlank()) etFirstPayment.error = null
                }
                insert.observe(viewLifecycleOwner) {
                    if (it) {
                        snackbar.apply { setText("Dues Created!") }.show()
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