package es.clcarras.mydues.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Dues::class], version = 1, exportSchema = false)
abstract class DuesRoomDatabase : RoomDatabase() {

    abstract fun duesDao(): DuesDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: DuesRoomDatabase? = null

        fun getDatabase(context: Context): DuesRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DuesRoomDatabase::class.java,
                    "due_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}