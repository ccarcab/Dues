package es.clcarras.mydues.database

import androidx.room.*

@Dao
interface DuesDao {

    @Query("SELECT * FROM dues")
    suspend fun getAll(): MutableList<Dues>

    @Query("SELECT COUNT(*) FROM dues")
    suspend fun getDueCount(): Int

    @Insert
    suspend fun insert(dues: Dues)

    @Insert
    suspend fun insertAll(dues: List<Dues>)

    @Delete
    suspend fun remove(dues: Dues)

    @Update
    suspend fun update(dues: Dues)

}