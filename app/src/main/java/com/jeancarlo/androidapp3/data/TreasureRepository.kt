package com.jeancarlo.androidapp3.data

/**
 * Repository keeps database-related work out of the Activities.
 */
class TreasureRepository(private val dao: TreasurePlaceDao) {

    suspend fun seedDatabaseIfNeeded() {
        if (dao.getAllPlaces().isEmpty()) {
            dao.insertAll(TreasureSeedData.places)
        }
    }

    suspend fun getAllPlaces(): List<TreasurePlace> = dao.getAllPlaces()

    suspend fun getPlaceById(id: Int): TreasurePlace? = dao.getPlaceById(id)

    suspend fun getCurrentPlace(): TreasurePlace? = dao.getCurrentPlace()

    suspend fun getVisitedCount(): Int = dao.getVisitedCount()

    suspend fun markVisited(id: Int) = dao.markVisited(id)

    suspend fun resetProgress() = dao.resetProgress()
}
