package es.clcarras.mydues.utils

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import vadiole.colorpicker.ColorPickerDialog
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

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

        private fun formatLocalDate(localDate: LocalDate): String =
            localDate.format(datePattern)

        private fun getLocalFromDate(dateToConvert: Date): LocalDate =
            Instant.ofEpochMilli(dateToConvert.time)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

        fun formatDate(date: Date): String = formatLocalDate(getLocalFromDate(date))

        fun getDate(year: Int, month: Int, day: Int): Date =
            Date.from(
                LocalDate
                    .of(year, month, day)
                    .atTime(12, 0)
                    .toInstant(ZoneId.systemDefault().rules.getOffset(LocalDateTime.now()))
            )

    }
}