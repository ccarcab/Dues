package es.clcarras.mydues.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import es.clcarras.mydues.model.Dues

@Dao
interface DuesDao {

    @Query("SELECT * FROM dues")
    suspend fun getAll(): List<Dues>

    @Query("SELECT COUNT(*) FROM dues")
    suspend fun getDueCount(): Int

    @Insert
    suspend fun insertAll(dues: List<Dues>)

    @Delete
    suspend fun delete(dues: Dues)

}