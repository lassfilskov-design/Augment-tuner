package com.augment.tuner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val distance: Float = 0f,          // km
    val maxSpeed: Float = 0f,          // km/h
    val avgSpeed: Float = 0f,          // km/h
    val startBattery: Int,             // %
    val endBattery: Int?,              // %
    val topSpeed: Float = 0f,          // Max speed achieved
    val duration: Long = 0,            // seconds
    val scooterMac: String,
    val notes: String = ""
) {
    fun getDurationMinutes(): Int = (duration / 60).toInt()

    fun getBatteryUsed(): Int {
        return endBattery?.let { startBattery - it } ?: 0
    }

    fun getAverageConsumption(): Float {
        val batteryUsed = getBatteryUsed()
        return if (distance > 0) batteryUsed / distance else 0f
    }
}
