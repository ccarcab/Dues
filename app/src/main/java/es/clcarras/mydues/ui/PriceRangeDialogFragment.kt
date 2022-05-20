package es.clcarras.mydues.ui

import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.DialogPriceRangeBinding
import es.clcarras.mydues.utils.Utility
import es.clcarras.mydues.viewmodel.PriceRangeDialogViewModel
import es.clcarras.mydues.viewmodel.PriceRangeDialogViewModel.Companion.END_DATE
import es.clcarras.mydues.viewmodel.PriceRangeDialogViewModel.Companion.INIT_DATE

class PriceRangeDialogFragment : DialogFragment() {

    private lateinit var binding: DialogPriceRangeBinding
    private lateinit var viewModel: PriceRangeDialogViewModel
    private lateinit var viewModelFactory: PriceRangeDialogViewModel.Factory

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogPriceRangeBinding.inflate(layoutInflater)
        viewModelFactory = PriceRangeDialogViewModel.Factory(
            resources.getStringArray(R.array.recurrence_array)
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[PriceRangeDialogViewModel::class.java]

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            with(binding) {

                etInitDate.setOnClickListener {
                    datePicker(INIT_DATE).show(childFragmentManager, DateDialogFragment.TAG)
                }
                etEndDate.setOnClickListener {
                    datePicker(END_DATE).show(childFragmentManager, DateDialogFragment.TAG)
                }

                initDate.observe(viewLifecycleOwner) {
                    etInitDate.setText(Utility.formatDate(it))
                }
                endDate.observe(viewLifecycleOwner) {
                    etEndDate.setText(Utility.formatDate(it))
                }
                totalPrice.observe(viewLifecycleOwner) {
                    animateTextView(it.toInt(), tvTotalPrice)
                }
            }
        }
    }

    private fun animateTextView(target: Int, textview: TextView) {
        val animator = ValueAnimator.ofInt(0, target)
        animator.duration = 1500
        animator.addUpdateListener {
            textview.text = it.animatedValue.toString()
        }
        animator.start()
    }
}