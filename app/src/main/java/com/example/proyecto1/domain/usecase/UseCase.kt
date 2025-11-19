package com.example.proyecto1.domain.usecase

import com.example.proyecto1.domain.model.ParkingSpot
import com.example.proyecto1.domain.model.Reservation
import com.example.proyecto1.domain.model.ReservationHistory
import com.example.proyecto1.domain.repository.FirebaseParkingRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// ==================== PARKING SPOTS ====================

class GetParkingSpotsUseCase {
    operator fun invoke(): Flow<List<ParkingSpot>> = callbackFlow {
        try {
            val firestore = FirebaseFirestore.getInstance()

            val listener = firestore.collection("parkingSpots")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    snapshot?.let { querySnapshot ->
                        val spots = querySnapshot.documents.mapNotNull { doc ->
                            try {
                                ParkingSpot(
                                    id = doc.id,
                                    basementNumber = (doc.getLong("basementNumber") ?: 0L).toInt(),
                                    totalSpaces = (doc.getLong("totalSpaces") ?: 0L).toInt(),
                                    occupiedSpaces = (doc.getLong("occupiedSpaces") ?: 0L).toInt(),
                                    latitude = doc.getDouble("latitude") ?: 0.0,
                                    longitude = doc.getDouble("longitude") ?: 0.0
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(spots)
                    }
                }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            close(e)
        }
    }
}

// ==================== RESERVATIONS ====================

class ReserveParkingSpotUseCase(
    private val parkingRepository: FirebaseParkingRepository = FirebaseParkingRepository()
) {
    suspend operator fun invoke(parkingSpotId: String, basementNumber: Int, userId: String): Result<Reservation> {
        return parkingRepository.createReservation(userId, parkingSpotId, basementNumber)
    }
}

class ConfirmArrivalUseCase(
    private val parkingRepository: FirebaseParkingRepository = FirebaseParkingRepository()
) {
    suspend operator fun invoke(reservationId: String, parkingSpotId: String): Result<Unit> {
        return parkingRepository.confirmArrival(reservationId, parkingSpotId)
    }
}

class CancelReservationUseCase(
    private val parkingRepository: FirebaseParkingRepository = FirebaseParkingRepository()
) {
    suspend operator fun invoke(reservationId: String, parkingSpotId: String): Result<Unit> {
        return parkingRepository.cancelReservation(reservationId, parkingSpotId)
    }
}

class MarkAsCompletedUseCase(
    private val parkingRepository: FirebaseParkingRepository = FirebaseParkingRepository()
) {
    suspend operator fun invoke(reservationId: String, parkingSpotId: String): Result<Unit> {
        return parkingRepository.markAsCompleted(reservationId, parkingSpotId)
    }
}

// ==================== HISTORY ====================

class GetReservationHistoryUseCase(
    private val parkingRepository: FirebaseParkingRepository = FirebaseParkingRepository()
) {
    suspend operator fun invoke(userId: String): Result<List<ReservationHistory>> {
        return parkingRepository.getReservationHistory(userId)
    }
}

// ==================== LEGACY (Mantener para compatibilidad) ====================

class LoginUseCase {
    suspend operator fun invoke(email: String, password: String): Result<com.example.proyecto1.domain.model.User> {
        return try {
            kotlinx.coroutines.delay(1000)
            if (email.isNotEmpty() && password.isNotEmpty()) {
                Result.success(
                    com.example.proyecto1.domain.model.User(
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

class RegisterUseCase {
    suspend operator fun invoke(name: String, email: String, password: String): Result<com.example.proyecto1.domain.model.User> {
        return try {
            kotlinx.coroutines.delay(1000)
            if (name.isNotEmpty() && email.isNotEmpty() && password.length >= 6) {
                Result.success(
                    com.example.proyecto1.domain.model.User(
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

class ForgotPasswordUseCase {
    suspend operator fun invoke(email: String): Result<Unit> {
        return try {
            kotlinx.coroutines.delay(1000)
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