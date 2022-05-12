package es.clcarras.mydues.model

import android.net.Uri

data class PreloadedDues (
    val name: String,
    val color: String,
    val image: Uri,
    val pkg: String
)