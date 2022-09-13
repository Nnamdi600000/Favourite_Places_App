package com.codennamdi.favouriteplacesapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavouritePlaceEntity::class], version = 1)
abstract class FavouritePlaceDatabase : RoomDatabase() {
    abstract fun favouritePlaceDao(): FavouritePlaceDao

    companion object {
        @Volatile
        private var INSTANCE: FavouritePlaceDatabase? = null

        fun getInstance(context: Context): FavouritePlaceDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        FavouritePlaceDatabase::class.java,
                        "student_database"
                    ).fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}