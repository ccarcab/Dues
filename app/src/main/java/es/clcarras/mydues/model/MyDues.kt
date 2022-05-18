package es.clcarras.mydues.model

import com.google.firebase.Timestamp
import java.util.*

data class MyDues(
    var id: String? = null,
    var price: Int = 0,
    var name: String? = null,
    var description: String? = null,
    var every: Int = 0,
    var recurrence: String? = null,
    var firstPayment: Date? = null,
    var paymentMethod: String? = null,
    var cardColor: Int = 0,
    var notificationUUID: String? = null,
    var `package`: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "price" to price,
        "name" to name,
        "description" to description,
        "every" to every,
        "recurrence" to recurrence,
        "firstPayment" to Timestamp(firstPayment!!),
        "paymentMethod" to paymentMethod,
        "cardColor" to cardColor,
        "notificationUUID" to notificationUUID,
        "package" to `package`
    )
}
