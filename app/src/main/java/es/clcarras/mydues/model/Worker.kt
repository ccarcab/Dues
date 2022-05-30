package es.clcarras.mydues.model

import com.google.firebase.Timestamp
import java.util.*

/**
 * Data Class para almacenar los datos de los workers
 */
data class Worker(
    var id: String? = null,
    var uuid: String? = null,
    var targetDate: Date? = null,
    var periodicity: Int = 0,
    var message: String? = null
) {

    /**
     * Método que devuelve los datos del data class en forma de mapa
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uuid" to uuid,
        "targetDate" to Timestamp(targetDate!!),
        "periodicity" to periodicity,
        "message" to message
    )
}