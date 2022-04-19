package es.clcarras.mydues.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import es.clcarras.mydues.model.Due

@Database(entities = [Due::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DueRoomDatabase : RoomDatabase() {

    abstract fun dueDao(): DueDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: DueRoomDatabase? = null

        fun getDatabase(context: Context): DueRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DueRoomDatabase::class.java,
                    "due_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}