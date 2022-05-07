package es.clcarras.mydues.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Dues(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var price: String,
    var name: String,
    var description: String? = null,
    var every: String,
    var recurrence: String,
    var firstPayment: String,
    var paymentMethod: String? = null,
    var cardColor: Int
)
