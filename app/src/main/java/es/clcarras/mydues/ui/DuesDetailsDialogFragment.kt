package es.clcarras.mydues.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.os.Bundle
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
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.utils.Utility
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.databinding.DuesDetailsDialogFragmentBinding
import es.clcarras.mydues.model.MyDues
import es.clcarras.mydues.viewmodel.DuesDetailsDialogViewModel
import es.clcarras.mydues.viewmodel.HomeViewModel
import java.time.ZoneId
import java.util.*
import kotlin.math.abs

class DuesDetailsDialogFragment(
    private val myDues: MyDues?,
    private val homeViewModel: HomeViewModel?
) : DialogFragment() {

    constructor() : this(null, null)

    private var _binding: DuesDetailsDialogFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DuesDetailsDialogViewModel
    private lateinit var viewModelFactory: DuesDetailsDialogViewModel.Factory

    companion object {
        const val TAG = "DuesDetailsDialogFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = DuesDetailsDialogFragmentBinding.inflate(layoutInflater)
        viewModelFactory = DuesDetailsDialogViewModel.Factory(
            DuesRoomDatabase.getDatabase(requireContext()), myDues, homeViewModel
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
            with(myDues) {
                if (this?.description?.isBlank() == true) tilDesc.visibility = View.GONE

                if (this?.paymentMethod?.isBlank() == true) tilPaymentMethod.visibility = View.GONE

            }
        }

        return binding.root
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        viewModel.close()
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
                cardColor.observe(viewLifecycleOwner) {
                    btnColorPicker.setBackgroundColor(it)
                    etPrice.backgroundTintList = ColorStateList.valueOf(it)
                    etPrice.setTextColor(Utility.contrastColor(it))
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
                    if (it.isNotBlank()) {
                        etFirstPayment.error = null
                        if (it != myDues?.firstPayment) {
                            updateNotification()
                        }
                    }
                }
                every.observe(viewLifecycleOwner) {
                    if (it != myDues?.every && it.isNotBlank()) {
                        updateNotification()
                    }
                }
                recurrence.observe(viewLifecycleOwner) {
                    if (it != myDues?.recurrence && it.isNotBlank()) {
                        updateNotification()
                    }
                }
                update.observe(viewLifecycleOwner) {
                    if (it) toggleViewEditMode()
                }
                delete.observe(viewLifecycleOwner) {
                    if (it) {
                        (requireActivity() as MainActivity).deleteWork(myDues?.notification!!)
                        dialog?.dismiss()
                    }
                }

            }
        }
    }

    private fun updateNotification() {
        with(requireActivity() as MainActivity) {
            val uuid = createWorkRequest(
                getString(
                    R.string.notification_msg,
                    myDues?.name, myDues?.price
                ), hoursUntilNextPayment()
            )
            deleteWork(viewModel.setNotification(uuid))
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

    private fun toggleViewEditMode() {
        with(binding) {
            container.children.forEach {
                if (it.visibility == View.GONE)
                    it.visibility = View.VISIBLE
                else if (it.visibility == View.VISIBLE)
                    if (it is Button || it is TextInputLayout && it.editText!!.text.isEmpty())
                        it.visibility = View.GONE

                if (it is TextInputLayout) it.editText?.isEnabled = !it.editText?.isEnabled!!
                if (it is EditText) it.isEnabled = !it.isEnabled
                if (it is Spinner) it.isEnabled = !it.isEnabled
            }
        }
    }

    private fun hoursUntilNextPayment(): Long {
        val timeUnits = resources.getStringArray(R.array.recurrence_array)
        with(viewModel) {
            val timeUnitValue = when (recurrence.value) {
                timeUnits[0] -> 1
                timeUnits[1] -> 7
                timeUnits[2] -> 30
                timeUnits[3] -> 365
                else -> 0
            }
            val totalTime = timeUnitValue * (every.value?.toInt() ?: 1)
            val nextPayment = Calendar.getInstance()
            val currentDate = Calendar.getInstance()
            // Se establece la fecha de primer pago
            nextPayment.time = Date.from(
                Utility.getLocalDateFromString(firstPayment.value!!)
                    .atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant()
            )
            // Se añade el tiempo hasta el próximo pago
            nextPayment.add(Calendar.DAY_OF_YEAR, totalTime)
            // Se calcula el tiempo que queda desde ahora hasta el próximo pago
            return (abs(nextPayment.time.time - currentDate.time.time) / 36e5).toLong()

        }
    }

}