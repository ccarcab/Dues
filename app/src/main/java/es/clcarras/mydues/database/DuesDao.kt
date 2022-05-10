package es.clcarras.mydues.database

import androidx.room.*
import es.clcarras.mydues.model.MyDues

@Dao
interface DuesDao {

    @Query("SELECT * FROM mydues")
    suspend fun getAll(): MutableList<MyDues>

    @Query("SELECT COUNT(*) FROM mydues")
    suspend fun getDueCount(): Int

    @Insert
    suspend fun insert(myDues: MyDues)

    @Insert
    suspend fun insertAll(dues: List<MyDues>)

    @Delete
    suspend fun remove(myDues: MyDues)

    @Update
    suspend fun update(myDues: MyDues)

}