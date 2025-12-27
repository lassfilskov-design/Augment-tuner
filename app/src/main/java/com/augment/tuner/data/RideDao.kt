package com.augment.tuner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {
    @Query("SELECT * FROM rides ORDER BY startTime DESC")
    fun getAllRides(): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE id = :rideId")
    suspend fun getRideById(rideId: Long): RideEntity?

    @Query("SELECT * FROM rides WHERE scooterMac = :mac ORDER BY startTime DESC")
    fun getRidesByScooter(mac: String): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE startTime >= :startDate ORDER BY startTime DESC")
    fun getRidesSince(startDate: Long): Flow<List<RideEntity>>

    @Query("SELECT SUM(distance) FROM rides")
    suspend fun getTotalDistance(): Float?

    @Query("SELECT COUNT(*) FROM rides")
    suspend fun getTotalRidesCount(): Int

    @Query("SELECT MAX(maxSpeed) FROM rides")
    suspend fun getTopSpeedEver(): Float?

    @Insert
    suspend fun insertRide(ride: RideEntity): Long

    @Update
    suspend fun updateRide(ride: RideEntity)

    @Delete
    suspend fun deleteRide(ride: RideEntity)

    @Query("DELETE FROM rides")
    suspend fun deleteAllRides()
}
