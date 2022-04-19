package es.clcarras.mydues.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import es.clcarras.mydues.model.Due

@Dao
interface DueDao {

    @Query("SELECT * FROM due")
    suspend fun getAll(): List<Due>

    @Query("SELECT COUNT(*) FROM due")
    suspend fun getDueCount(): Int

    @Insert
    suspend fun insertAll(dues: List<Due>)

    @Delete
    suspend fun delete(due: Due)

}