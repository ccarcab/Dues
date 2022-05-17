package es.clcarras.mydues.ui

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.icu.util.Calendar
import android.net.Uri
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
import com.squareup.picasso.Picasso
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.utils.Utility
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
        viewModelFactory = NewDuesViewModel.Factory( args,
            getColor(requireContext(), R.color.default_card_color)
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[NewDuesViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setOnTextChanged()
        setOnClickListeners()
        setSpinner()
        setObservers()

        viewModel.checkSelectedDues()

        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setFabAction()
        (requireActivity() as MainActivity).getBottomAppBar().performHide(true)
    }

    private fun setFabAction() {
        with((requireActivity() as MainActivity).getFab()) {
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

    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun setObservers() {
        with(binding) {
            with(viewModel!!) {
                preloadDues.observe(viewLifecycleOwner) {
                    etName.isEnabled = it == null
                    if (it != null) {
                        with(binding) {
                            ivPreloadDues.visibility = View.VISIBLE
                            Picasso.get().load(Uri.parse(it.image)).into(ivPreloadDues)
                            ivPreloadDues.setColorFilter(Utility.contrastColor(it.color))
                            etName.setText(it.name)
                        }
                    }
                }
                cardColor.observe(viewLifecycleOwner) {
                    btnColorPicker.setBackgroundColor(it)
                    btnColorPicker.setTextColor(Utility.contrastColor(it))
                    btnColorPicker.compoundDrawableTintList = ColorStateList.valueOf(Utility.contrastColor(it))
                    etPrice.backgroundTintList = ColorStateList.valueOf(it)
                    etPrice.setTextColor(Utility.contrastColor(it))
                    etPrice.setHintTextColor(Utility.contrastColor(it))
                    tvCurrency.backgroundTintList = ColorStateList.valueOf(it)
                    tvCurrency.setTextColor(Utility.contrastColor(it))
                    if (ivPreloadDues.visibility == View.VISIBLE)
                        ivPreloadDues.setColorFilter(Utility.contrastColor(it))
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
                    if (it != null) {
                        etFirstPayment.error = null
                        etFirstPayment.setText("$it")
                    }
                }
                validInput.observe(viewLifecycleOwner) {
                    if (it) {
                        val uuid = (requireActivity() as MainActivity).createWorkRequest(
                            getString(
                                R.string.notification_msg,
                                name.value, price.value.toString()
                            ),
                            (periodicityInHours() * 36e5).toLong(),
                            millisUntilNextPayment()
                        ).toString()
                        saveDues(uuid)
                    }
                }
                insert.observe(viewLifecycleOwner) {
                    if (it) {
                        snackbar.apply { setText("Dues Created!") }.show()
                        findNavController().navigate(R.id.nav_home)
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

    private fun millisUntilNextPayment(): Long {
            val nextPayment = Calendar.getInstance()
            val currentDate = Calendar.getInstance()
            // Se establece la fecha de primer pago
            nextPayment.time = viewModel.firstPayment.value!!
            // Se añade el tiempo hasta el próximo pago
            nextPayment.add(Calendar.HOUR_OF_DAY, periodicityInHours())
            // Se calcula el tiempo que queda desde ahora hasta el próximo pago
            return abs(nextPayment.time.time - currentDate.time.time)


    }

    private fun periodicityInHours(): Int {
        with(viewModel) {
            val timeUnits = resources.getStringArray(R.array.recurrence_array)
            val period = when (viewModel.recurrence.value) {
                timeUnits[0] -> 1
                timeUnits[1] -> 7
                timeUnits[2] -> 30
                timeUnits[3] -> 365
                else -> 0
            } * (every.value?.toInt() ?: 1) * 24
            return period
        }
    }

}