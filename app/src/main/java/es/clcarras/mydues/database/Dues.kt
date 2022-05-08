package es.clcarras.mydues.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Dues(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var price: String,
    var name: String,
    var description: String = "",
    var every: String,
    var recurrence: String,
    var firstPayment: String,
    var paymentMethod: String = "",
    var cardColor: Int,
    var notification: UUID
)
