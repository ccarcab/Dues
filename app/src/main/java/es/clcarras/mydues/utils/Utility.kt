package es.clcarras.mydues.utils

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import es.clcarras.mydues.model.MyDues
import vadiole.colorpicker.ColorPickerDialog
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Clase que contiene una serie de métodos estáticos que otorgan utilidad a la aplicación
 */
class Utility {
    companion object {

        const val TAG = "Utility"

        /**
         * Función para obtener el color de contraste del color recibido por parámetros
         */
        fun contrastColor(color: Int): Int =
            if (ColorUtils.calculateLuminance(color) < 0.5) Color.WHITE
            else Color.BLACK

        /**
         * Método que devuelve un cuadro de diálogo
         */
        fun colorPicker(currentColor: Int?) =
            ColorPickerDialog.Builder()
                .setInitialColor(currentColor ?: Color.WHITE)
                .setColorModel(vadiole.colorpicker.ColorModel.HSV)
                .setColorModelSwitchEnabled(true)
                .setButtonOkText(android.R.string.ok)
                .setButtonCancelText(android.R.string.cancel)

        // Patrón para darle formato a una fecha
        private val datePattern = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        // Método que le da formato a una fecha LocalDate
        private fun formatLocalDate(localDate: LocalDate): String =
            localDate.format(datePattern)

        // Método para transformar una fecha Date a un fecha LocalDate
        private fun getLocalFromDate(dateToConvert: Date): LocalDate =
            Instant.ofEpochMilli(dateToConvert.time)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

        /**
         * Método que le da formato a una fecha y la devuelve en forma de String
         */
        fun formatDate(date: Date): String = formatLocalDate(getLocalFromDate(date))

        /**
         * Método que calcula y devuelve el precio de una cuota a partir de una recurrencia
         */
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

        // Método para calcular el precio anual de una cuota
        private fun calculateYearlyPrice(myDues: MyDues, recurrences: Array<String>) =
            when (myDues.recurrence) {
                recurrences[0] -> myDues.price * 365
                recurrences[1] -> myDues.price * 52
                recurrences[2] -> myDues.price * 12
                else -> myDues.price
            } / myDues.every

        /**
         * Método para calcular el precio mensual de una cuota
         */
        fun calculateMonthlyPrice(myDues: MyDues, recurrences: Array<String>) =
            when (myDues.recurrence) {
                recurrences[0] -> myDues.price * 30
                recurrences[1] -> myDues.price * 4.2
                recurrences[3] -> myDues.price / 12
                else -> myDues.price
            } / myDues.every

        // Método para calcular el precio semanal de una cuota
        private fun calculateWeeklyPrice(myDues: MyDues, recurrences: Array<String>) =
            when (myDues.recurrence) {
                recurrences[0] -> myDues.price * 7
                recurrences[2] -> myDues.price / 4.2
                recurrences[3] -> myDues.price / 52
                else -> myDues.price
            } / myDues.every

        /**
         * Método para calcular el precio diario de una cuota
         */
        fun calculateDailyPrice(myDues: MyDues, recurrences: Array<String>) =
            when (myDues.recurrence) {
                recurrences[1] -> myDues.price / 7
                recurrences[2] -> myDues.price / 30
                recurrences[3] -> myDues.price / 365
                else -> myDues.price
            } / myDues.every

    }
}