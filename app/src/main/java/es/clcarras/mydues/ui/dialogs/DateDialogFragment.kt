package es.clcarras.mydues.ui.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class DateDialogFragment : DialogFragment() {

    private var listener: DatePickerDialog.OnDateSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireActivity(), listener, year, month, day)
    }

    companion object {
        const val TAG = "DateDialogFragment"
        fun newInstance(listener: DatePickerDialog.OnDateSetListener): DateDialogFragment {
            val fragment = DateDialogFragment()
            fragment.listener = listener
            return fragment
        }
    }

}