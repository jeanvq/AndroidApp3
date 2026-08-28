package com.jeancarlo.androidapp3.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object used by Room.
 * All functions are suspend functions so database work runs off the main UI thread.
 */
@Dao
interface TreasurePlaceDao {

    @Query("SELECT * FROM treasure_places ORDER BY huntOrder ASC")
    suspend fun getAllPlaces(): List<TreasurePlace>

    @Query("SELECT * FROM treasure_places WHERE id = :id LIMIT 1")
    suspend fun getPlaceById(id: Int): TreasurePlace?

    @Query("SELECT * FROM treasure_places WHERE isVisited = 0 ORDER BY huntOrder ASC LIMIT 1")
    suspend fun getCurrentPlace(): TreasurePlace?

    @Query("SELECT COUNT(*) FROM treasure_places WHERE isVisited = 1")
    suspend fun getVisitedCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(places: List<TreasurePlace>)

    @Query("UPDATE treasure_places SET isVisited = 1 WHERE id = :id")
    suspend fun markVisited(id: Int)

    @Query("UPDATE treasure_places SET isVisited = 0")
    suspend fun resetProgress()
}
