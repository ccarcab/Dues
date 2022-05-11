package es.clcarras.mydues.ui

import android.content.res.ColorStateList
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.utils.Utility
import es.clcarras.mydues.database.DuesRoomDatabase
import es.clcarras.mydues.databinding.NewDuesFragmentBinding
import es.clcarras.mydues.viewmodel.NewDuesViewModel
import java.time.ZoneId
import java.util.*
import kotlin.math.abs

class NewDuesFragment : Fragment() {

    private lateinit var binding: NewDuesFragmentBinding
    private lateinit var viewModel: NewDuesViewModel
    private lateinit var viewModelFactory: NewDuesViewModel.Factory

    private lateinit var snackbar: Snackbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NewDuesFragmentBinding.inflate(inflater, container, false)
        val args = NewDuesFragmentArgs.fromBundle(requireArguments())
        viewModelFactory = NewDuesViewModel.Factory(
            DuesRoomDatabase.getDatabase(requireContext()), args,
            getColor(requireContext(), R.color.default_card_color)
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[NewDuesViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.etName.isEnabled = args.name.isEmpty()

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
            setOnClickListener { viewModel.checkData() }
            show()
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
                cardColor.observe(viewLifecycleOwner) {
                    btnColorPicker.setBackgroundColor(it)
                    btnColorPicker.setTextColor(Utility.contrastColor(it))
                    etPrice.backgroundTintList = ColorStateList.valueOf(it)
                    etPrice.setTextColor(Utility.contrastColor(it))
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
                validInput.observe(viewLifecycleOwner) {
                    if (it) {
                        val uuid = (requireActivity() as MainActivity).createWorkRequest(
                            getString(R.string.notification_msg,
                                name.value, price.value
                            ), hoursUntilNextPayment()
                        )
                        saveDues(uuid)
                    }
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