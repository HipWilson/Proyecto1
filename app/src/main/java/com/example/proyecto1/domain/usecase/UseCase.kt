package com.example.proyecto1.domain.usecase

import android.util.Log
import com.example.proyecto1.domain.model.ParkingSpot
import com.example.proyecto1.domain.model.Reservation
import com.example.proyecto1.domain.model.ReservationHistory
import com.example.proyecto1.domain.repository.FirebaseParkingRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

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

class GetActiveReservationUseCase {
    suspend operator fun invoke(userId: String): Result<Reservation?> {
        return try {
            val firestore = FirebaseFirestore.getInstance()

            val snapshot = firestore.collection("reservations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val doc = snapshot.documents[0]
                val reservation = Reservation(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    parkingSpotId = doc.getString("parkingSpotId") ?: "",
                    basementNumber = (doc.getLong("basementNumber") ?: 0L).toInt(),
                    startTime = doc.getLong("startTime") ?: 0L,
                    expirationTime = doc.getLong("expirationTime") ?: 0L,
                    isActive = doc.getBoolean("isActive") ?: false,
                    isConfirmed = doc.getBoolean("isConfirmed") ?: false,
                    isCompleted = doc.getBoolean("isCompleted") ?: false
                )
                Log.d("GetActiveReservationUseCase", "Reserva activa encontrada: ${reservation.id}")
                Result.success(reservation)
            } else {
                Log.d("GetActiveReservationUseCase", "No hay reservas activas para $userId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e("GetActiveReservationUseCase", "Error obteniendo reserva activa", e)
            Result.failure(e)
        }
    }
}

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

class GetReservationHistoryUseCase(
    private val parkingRepository: FirebaseParkingRepository = FirebaseParkingRepository()
) {
    suspend operator fun invoke(userId: String): Result<List<ReservationHistory>> {
        return parkingRepository.getReservationHistory(userId)
    }
}

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