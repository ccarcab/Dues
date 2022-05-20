package es.clcarras.mydues.utils

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import es.clcarras.mydues.model.MyDues
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


        fun calculatePrice(currentRecurrence: String, myDues: MyDues, recurrences: Array<String>) =
            when (currentRecurrence) {
                recurrences[0] ->
                    calculateDailyPrice(myDues, recurrences)

                recurrences[1] ->
                    calculateWeeklyPrice(myDues, recurrences)

                recurrences[2] ->
                    calculateMonthlyPrice(myDues, recurrences)

                recurrences[3] ->
                    calculateYearlyPrice(myDues, recurrences)
                else -> 0.0
            }

        private fun calculateYearlyPrice(myDues: MyDues, recurrences: Array<String>) =
            when (myDues.recurrence) {
                recurrences[0] -> myDues.price * 365
                recurrences[1] -> myDues.price * 52
                recurrences[2] -> myDues.price * 12
                else -> myDues.price
            } / myDues.every.toDouble()

        fun calculateMonthlyPrice(myDues: MyDues, recurrences: Array<String>) =
            when (myDues.recurrence) {
                recurrences[0] -> myDues.price * 30
                recurrences[1] -> myDues.price * 4.2
                recurrences[3] -> myDues.price / 12
                else -> myDues.price
            } / myDues.every.toDouble()

        private fun calculateWeeklyPrice(myDues: MyDues, recurrences: Array<String>) =
            when (myDues.recurrence) {
                recurrences[0] -> myDues.price * 7
                recurrences[2] -> myDues.price / 4.2
                recurrences[3] -> myDues.price / 52
                else -> myDues.price
            } / myDues.every.toDouble()

        private fun calculateDailyPrice(myDues: MyDues, recurrences: Array<String>) =
            when (myDues.recurrence) {
                recurrences[1] -> myDues.price / 7
                recurrences[2] -> myDues.price / 30
                recurrences[3] -> myDues.price / 365
                else -> myDues.price
            } / myDues.every.toDouble()

    }
}