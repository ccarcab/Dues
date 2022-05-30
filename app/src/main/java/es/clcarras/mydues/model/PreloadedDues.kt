package es.clcarras.mydues.model

/**
 * Data Class para almacenar los datos de las cuotas precargadas
 */
data class PreloadedDues (
    val name: String? = null,
    val color: Int = 0,
    val image: String? = null,
    val `package`: String? = null
)