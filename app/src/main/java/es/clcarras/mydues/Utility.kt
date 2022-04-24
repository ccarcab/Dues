package es.clcarras.mydues

import android.graphics.Color
import androidx.core.graphics.ColorUtils

class Utility {
    companion object {
        fun contrastColor(color: Int): Int =
            if (ColorUtils.calculateLuminance(color) < 0.5) Color.WHITE
            else Color.BLACK
    }
}