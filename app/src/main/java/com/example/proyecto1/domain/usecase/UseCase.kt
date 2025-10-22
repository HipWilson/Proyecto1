package com.example.proyecto1.domain.usecase

import com.example.proyecto1.domain.model.ParkingSpot
import com.example.proyecto1.domain.model.Reservation
import com.example.proyecto1.domain.model.ReservationHistory
import com.example.proyecto1.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Login Use Case
class LoginUseCase {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return try {
            delay(1000) // Simular llamada de red
            if (email.isNotEmpty() && password.isNotEmpty()) {
                Result.success(
                    User(
                        id = "user_${System.currentTimeMillis()}",
                        name = "Usuario Demo",
                        email = email
                    )
                )
            } else {
                Result.failure(Exception("Credenciales inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Register Use Case
class RegisterUseCase {
    suspend operator fun invoke(name: String, email: String, password: String): Result<User> {
        return try {
            delay(1000)
            if (name.isNotEmpty() && email.isNotEmpty() && password.length >= 6) {
                Result.success(
                    User(
                        id = "user_${System.currentTimeMillis()}",
                        name = name,
                        email = email
                    )
                )
            } else {
                Result.failure(Exception("Datos inválidos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Forgot Password Use Case
class ForgotPasswordUseCase {
    suspend operator fun invoke(email: String): Result<Unit> {
        return try {
            delay(1000)
            if (email.isNotEmpty()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Email inválido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Parking Spots Use Case
class GetParkingSpotsUseCase {
    operator fun invoke(): Flow<List<ParkingSpot>> = flow {
        while (true) {
            val spots = listOf(
                ParkingSpot(
                    id = "parking_1",
                    basementNumber = 1,
                    totalSpaces = 50,
                    availableSpaces = (0..50).random(),
                    latitude = 14.6037,
                    longitude = -90.4887
                ),
                ParkingSpot(
                    id = "parking_2",
                    basementNumber = 2,
                    totalSpaces = 45,
                    availableSpaces = (0..45).random(),
                    latitude = 14.6040,
                    longitude = -90.4890
                ),
                ParkingSpot(
                    id = "parking_3",
                    basementNumber = 3,
                    totalSpaces = 40,
                    availableSpaces = (0..40).random(),
                    latitude = 14.6043,
                    longitude = -90.4893
                ),
                ParkingSpot(
                    id = "parking_4",
                    basementNumber = 4,
                    totalSpaces = 35,
                    availableSpaces = (0..35).random(),
                    latitude = 14.6046,
                    longitude = -90.4896
                )
            )
            emit(spots)
            delay(5000) // Actualizar cada 5 segundos
        }
    }
}

// Reserve Parking Spot Use Case
class ReserveParkingSpotUseCase {
    suspend operator fun invoke(parkingSpotId: String, basementNumber: Int): Result<Reservation> {
        return try {
            delay(500)
            val now = System.currentTimeMillis()
            Result.success(
                Reservation(
                    id = "reservation_${System.currentTimeMillis()}",
                    userId = "current_user",
                    parkingSpotId = parkingSpotId,
                    basementNumber = basementNumber,
                    startTime = now,
                    expirationTime = now + (5 * 60 * 1000), // 5 minutos
                    isActive = true,
                    isConfirmed = false
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Confirm Arrival Use Case
class ConfirmArrivalUseCase {
    suspend operator fun invoke(reservationId: String): Result<Unit> {
        return try {
            delay(500)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Cancel Reservation Use Case
class CancelReservationUseCase {
    suspend operator fun invoke(reservationId: String): Result<Unit> {
        return try {
            delay(500)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Get Reservation History Use Case
class GetReservationHistoryUseCase {
    suspend operator fun invoke(userId: String): Result<List<ReservationHistory>> {
        return try {
            delay(1000)
            val history = listOf(
                ReservationHistory(
                    id = "history_1",
                    basementNumber = 1,
                    date = System.currentTimeMillis() - (24 * 60 * 60 * 1000),
                    wasConfirmed = true
                ),
                ReservationHistory(
                    id = "history_2",
                    basementNumber = 3,
                    date = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000),
                    wasConfirmed = false
                )
            )
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}