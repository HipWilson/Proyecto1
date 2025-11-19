package com.example.proyecto1.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = ""
)

data class ParkingSpot(
    val id: String = "",
    val basementNumber: Int = 0,
    val totalSpaces: Int = 0,
    val occupiedSpaces: Int = 0, // Cambio: ahora es occupiedSpaces, no availableSpaces
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    val availableSpaces: Int
        get() = totalSpaces - occupiedSpaces

    val status: ParkingStatus
        get() = when {
            availableSpaces == 0 -> ParkingStatus.FULL
            availableSpaces <= totalSpaces * 0.2 -> ParkingStatus.FEW_SPOTS
            else -> ParkingStatus.AVAILABLE
        }

    val occupancyPercentage: Float
        get() = if (totalSpaces > 0) {
            (occupiedSpaces.toFloat() / totalSpaces.toFloat()) * 100
        } else 0f
}

enum class ParkingStatus {
    AVAILABLE,
    FEW_SPOTS,
    FULL
}

data class Reservation(
    val id: String = "",
    val userId: String = "",
    val parkingSpotId: String = "",
    val basementNumber: Int = 0,
    val startTime: Long = 0L,
    val expirationTime: Long = 0L,
    val isActive: Boolean = false,
    val isConfirmed: Boolean = false,
    val isCompleted: Boolean = false
) {
    val remainingMinutes: Int
        get() {
            val now = System.currentTimeMillis()
            val remaining = (expirationTime - now) / 1000 / 60
            return remaining.toInt().coerceAtLeast(0)
        }

    val isExpired: Boolean
        get() = System.currentTimeMillis() > expirationTime
}

data class ReservationHistory(
    val id: String = "",
    val userId: String = "",
    val basementNumber: Int = 0,
    val date: Long = 0L,
    val wasConfirmed: Boolean = false,
    val duration: Long = 0L // en minutos
)