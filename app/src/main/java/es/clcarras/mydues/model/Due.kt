package es.clcarras.mydues.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Due(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: String
)
