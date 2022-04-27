package es.clcarras.mydues

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentManager
import vadiole.colorpicker.ColorPickerDialog

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

    }
}