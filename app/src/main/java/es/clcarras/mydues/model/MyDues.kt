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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MyDues

        if (name != other.name) return false
        if (image != other.image) return false
        if (pkg != other.pkg) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + pkg.hashCode()
        return result
    }
}
