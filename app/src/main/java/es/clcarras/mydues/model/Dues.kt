package es.clcarras.mydues.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Dues(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val price: String,
    val name: String,
    val description: String? = null,
    val recurrence: String,
    val firstPayment: String,
    val paymentMethod: String? = null,
    val cardColor: Int
)
