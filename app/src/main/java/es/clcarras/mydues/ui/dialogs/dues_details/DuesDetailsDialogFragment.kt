package es.clcarras.mydues.ui.dialogs.dues_details

import android.app.Dialog
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import es.clcarras.mydues.R
import es.clcarras.mydues.Utility
import es.clcarras.mydues.database.DuesRoomDatabase
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

    private lateinit var viewModel: DuesDetailsDialogViewModel
    private lateinit var viewModelFactory: DuesDetailsDialogViewModelFactory

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
        viewModelFactory = DuesDetailsDialogViewModelFactory(
            DuesRoomDatabase.getDatabase(requireContext()), dues
        )
        viewModel =
            ViewModelProvider(this, viewModelFactory)[DuesDetailsDialogViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setOnTextChanged()
        setOnClickListeners()
        setObservers()
        setSpinner()

        with(binding) {
            with(dues) {
                if (description.isBlank()) tilDesc.visibility = View.GONE

                if (paymentMethod.isBlank()) tilPaymentMethod.visibility = View.GONE

                container.setBackgroundColor(cardColor)
            }
        }

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
            btnClose.setOnClickListener { dialog?.dismiss() }
            btnEdit.setOnClickListener { toggleViewEditMode() }
        }

    }

    private fun setObservers() {
        with(binding) {
            with(viewModel!!) {
                contrastColor.observe(viewLifecycleOwner) {
                    setContrast(it)
                }
                cardColor.observe(viewLifecycleOwner) {
                    btnColorPicker.setBackgroundColor(it)
                    container.setBackgroundColor(it)
                }
                error.observe(viewLifecycleOwner) {
                    if (it.isNotBlank()) {
                        Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        if (etPrice.text.isNullOrBlank()) etPrice.error = "Required"
                        if (etName.text.isNullOrBlank()) etName.error = "Required"
                        if (etFirstPayment.text.isNullOrBlank()) etFirstPayment.error = "Required"
                    }
                }
                firstPayment.observe(viewLifecycleOwner) {
                    if (it.isNotBlank()) etFirstPayment.error = null
                }
                update.observe(viewLifecycleOwner) {
                    if (it) {
                        fragment.updateDues(dues)
                        Snackbar.make(requireView(), "Dues Updated!", Snackbar.LENGTH_LONG).show()
                        toggleViewEditMode()
                    }
                }
                delete.observe(viewLifecycleOwner) {
                    if (it) {
                        fragment.deleteDues(dues)
                        dialog?.dismiss()
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

    private fun setContrast(contrastColor: Int) {
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
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        dialog?.dismiss()
        super.onConfigurationChanged(newConfig)
    }

}