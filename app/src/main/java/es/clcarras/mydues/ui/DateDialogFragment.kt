package es.clcarras.mydues.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

class DateDialogFragment(
    private val initialDate: Date?
) : DialogFragment() {

    constructor() : this(null)

    private var listener: DatePickerDialog.OnDateSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        if (initialDate != null)
            c.time = initialDate

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireActivity(), listener, year, month, day)
    }

    companion object {
        const val TAG = "DateDialogFragment"
        fun newInstance(
            initialDate: Date?,
            listener: DatePickerDialog.OnDateSetListener
        ): DateDialogFragment {
            val fragment = DateDialogFragment(initialDate)
            fragment.listener = listener
            return fragment
        }
    }

}