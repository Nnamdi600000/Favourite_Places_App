package com.codennamdi.favouriteplacesapp.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouritePlaceDao {
    @Insert
    suspend fun insert(favouritePlaceEntity: FavouritePlaceEntity)

    @Update
    suspend fun update(favouritePlaceEntity: FavouritePlaceEntity)

    @Delete
    suspend fun delete(favouritePlaceEntity: FavouritePlaceEntity)

    @Query("SELECT * FROM  `favouritePlaces-table`")
    fun fetchAllFavouritePlaces(): Flow<List<FavouritePlaceEntity>>

    @Query("SELECT * FROM  `favouritePlaces-table` where id=:id")
    fun fetchFavouritePlacesById(id: Int): Flow<FavouritePlaceEntity>
}