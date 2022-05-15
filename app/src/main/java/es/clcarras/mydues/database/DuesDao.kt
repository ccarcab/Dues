package es.clcarras.mydues.database

import androidx.room.*
import es.clcarras.mydues.model.MyDues

@Dao
interface DuesDao {

    @Query("SELECT * FROM mydues")
    suspend fun getAll(): MutableList<MyDues>

    @Query("SELECT * FROM mydues WHERE pkg != \"\"")
    suspend fun getPreloadDues(): MutableList<MyDues>

    @Insert
    suspend fun insert(myDues: MyDues)

    @Delete
    suspend fun remove(myDues: MyDues)

    @Update
    suspend fun update(myDues: MyDues)

}