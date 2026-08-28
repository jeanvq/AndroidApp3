package com.jeancarlo.androidapp3.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents one stop in the Cambridge treasure hunt.
 *
 * huntOrder controls the sequence of the game.
 * isVisited is stored in Room so progress survives app restarts.
 */
@Entity(tableName = "treasure_places")
data class TreasurePlace(
    @PrimaryKey val id: Int,
    val huntOrder: Int,
    val name: String,
    val address: String,
    val clue: String,
    val latitude: Double,
    val longitude: Double,
    val isVisited: Boolean = false
)
