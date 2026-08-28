package com.jeancarlo.androidapp3.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Single Room database for the treasure hunt.
 * A singleton prevents accidentally creating several database instances.
 */
@Database(
    entities = [TreasurePlace::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun treasurePlaceDao(): TreasurePlaceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cambridge_treasure_hunt.db"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
