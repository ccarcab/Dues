package es.clcarras.mydues.model

import com.google.firebase.Timestamp
import java.util.*

data class Worker(
    var id: String? = null,
    var uuid: String? = null,
    var targetDate: Date? = null,
    var periodicity: Int = 0,
    var message: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uuid" to uuid,
        "targetDate" to Timestamp(targetDate!!),
        "periodicity" to periodicity,
        "message" to message
    )
}