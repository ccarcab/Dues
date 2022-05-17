package es.clcarras.mydues.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class MyDues(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var price: String,
    var name: String,
    var description: String = "",
    var every: String,
    var recurrence: String,
    var firstPayment: String,
    var paymentMethod: String = "",
    var cardColor: Int,
    var image: String = "",
    var pkg: String = "",
    var notification: UUID
)
