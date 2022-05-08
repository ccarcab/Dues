package es.clcarras.mydues.ui.dialogs.dues_details

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import es.clcarras.mydues.ui.home.HomeViewModel

class DuesDetailsDialogFragment(
    private val dues: Dues?,
    private val homeViewModel: HomeViewModel?
) : DialogFragment() {

    constructor() : this(null, null)

    private var _binding: DuesDetailsDialogFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DuesDetailsDialogViewModel
    private lateinit var viewModelFactory: DuesDetailsDialogViewModelFactory

    companion object {
        const val TAG = "DuesDetailsDialogFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = DuesDetailsDialogFragmentBinding.inflate(layoutInflater)
        viewModelFactory = DuesDetailsDialogViewModelFactory(
            DuesRoomDatabase.getDatabase(requireContext()), dues, homeViewModel
        )
        viewModel =
            ViewModelProvider(this, viewModelFactory)[DuesDetailsDialogViewModel::class.java]
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setOnTextChanged()
        setOnClickListeners()
        setObservers()
        setSpinner()

        with(binding) {
            with(dues) {
                if (this?.description?.isBlank() == true) tilDesc.visibility = View.GONE

                if (this?.paymentMethod?.isBlank() == true) tilPaymentMethod.visibility = View.GONE

                container.setBackgroundColor(this?.cardColor ?: 0)
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
            btnClose.setOnClickListener {
                viewModel!!.close()
                dialog?.dismiss()
            }
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
                        Snackbar.make(requireView(), "Dues Updated!", Snackbar.LENGTH_LONG).show()
                        toggleViewEditMode()
                    }
                }
                delete.observe(viewLifecycleOwner) {
                    if (it) dialog?.dismiss()
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
                spRecurrence.isEnabled = !spRecurrence.isEnabled
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

}