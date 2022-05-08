package es.clcarras.mydues

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentManager
import vadiole.colorpicker.ColorPickerDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class Utility {
    companion object {

        const val TAG = "Utility"

        fun contrastColor(color: Int): Int =
            if (ColorUtils.calculateLuminance(color) < 0.5) Color.WHITE
            else Color.BLACK

        fun colorPicker(currentColor: Int?) =
            ColorPickerDialog.Builder()
                .setInitialColor(currentColor ?: Color.WHITE)
                .setColorModel(vadiole.colorpicker.ColorModel.HSV)
                .setColorModelSwitchEnabled(true)
                .setButtonOkText(android.R.string.ok)
                .setButtonCancelText(android.R.string.cancel)

        private val datePattern = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        fun formatLocalDate(localDate: LocalDate): String =
            localDate.format(datePattern)

        private fun getLocalDateFromString(d: String): LocalDate =
            LocalDate.parse(d, datePattern)

        fun getDaysDif(fromDate: String, toDate: String): Long =
            ChronoUnit.DAYS.between(getLocalDateFromString(fromDate), getLocalDateFromString(toDate))

    }
}