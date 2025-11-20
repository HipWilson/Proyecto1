package com.example.proyecto1.domain.repository

import android.util.Log
import com.example.proyecto1.domain.model.ParkingSpot
import com.example.proyecto1.domain.model.Reservation
import com.example.proyecto1.domain.model.ReservationHistory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

interface IParkingRepository {
    suspend fun createReservation(userId: String, parkingSpotId: String, basementNumber: Int): Result<Reservation>
    suspend fun confirmArrival(reservationId: String, parkingSpotId: String): Result<Unit>
    suspend fun markAsCompleted(reservationId: String, parkingSpotId: String): Result<Unit>
    suspend fun cancelReservation(reservationId: String, parkingSpotId: String): Result<Unit>
    suspend fun getReservationHistory(userId: String): Result<List<ReservationHistory>>
}

class FirebaseParkingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IParkingRepository {

    companion object {
        private const val TAG = "FirebaseParkingRepository"
        private const val PARKING_COLLECTION = "parkingSpots"
        private const val RESERVATIONS_COLLECTION = "reservations"
        private const val HISTORY_COLLECTION = "reservationHistory"
    }

    override suspend fun createReservation(
        userId: String,
        parkingSpotId: String,
        basementNumber: Int
    ): Result<Reservation> {
        return try {
            val now = System.currentTimeMillis()
            val reservationId = firestore.collection(RESERVATIONS_COLLECTION).document().id

            val reservation = Reservation(
                id = reservationId,
                userId = userId,
                parkingSpotId = parkingSpotId,
                basementNumber = basementNumber,
                startTime = now,
                expirationTime = now + (5 * 60 * 1000),
                isActive = true,
                isConfirmed = false,
                isCompleted = false
            )

            firestore.collection(RESERVATIONS_COLLECTION).document(reservationId).set(
                mapOf(
                    "userId" to userId,
                    "parkingSpotId" to parkingSpotId,
                    "basementNumber" to basementNumber,
                    "startTime" to now,
                    "expirationTime" to reservation.expirationTime,
                    "isActive" to true,
                    "isConfirmed" to false,
                    "isCompleted" to false
                )
            ).await()

            firestore.collection(PARKING_COLLECTION).document(parkingSpotId)
                .update("occupiedSpaces", FieldValue.increment(1)).await()

            Log.d(TAG, "Reservación creada: $reservationId")
            Result.success(reservation)
        } catch (e: Exception) {
            Log.e(TAG, "Error creando reservación", e)
            Result.failure(e)
        }
    }

    override suspend fun confirmArrival(reservationId: String, parkingSpotId: String): Result<Unit> {
        return try {
            firestore.collection(RESERVATIONS_COLLECTION).document(reservationId)
                .update("isConfirmed", true).await()

            Log.d(TAG, "Llegada confirmada: $reservationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error confirmando llegada", e)
            Result.failure(e)
        }
    }

    override suspend fun markAsCompleted(reservationId: String, parkingSpotId: String): Result<Unit> {
        return try {
            val doc = firestore.collection(RESERVATIONS_COLLECTION).document(reservationId).get().await()
            val reservation = doc.data

            if (reservation != null) {
                val userId = reservation["userId"] as? String ?: return Result.failure(Exception("Usuario no encontrado"))
                val basementNumber = (reservation["basementNumber"] as? Long)?.toInt() ?: 0
                val startTime = reservation["startTime"] as? Long ?: System.currentTimeMillis()
                val isConfirmed = reservation["isConfirmed"] as? Boolean ?: false

                firestore.collection(PARKING_COLLECTION).document(parkingSpotId)
                    .update("occupiedSpaces", FieldValue.increment(-1)).await()

                firestore.collection(RESERVATIONS_COLLECTION).document(reservationId)
                    .update(mapOf(
                        "isCompleted" to true,
                        "isActive" to false
                    )).await()

                val historyId = firestore.collection(HISTORY_COLLECTION).document().id
                val duration = (System.currentTimeMillis() - startTime) / 1000 / 60

                firestore.collection(HISTORY_COLLECTION).document(historyId).set(
                    mapOf(
                        "userId" to userId,
                        "basementNumber" to basementNumber,
                        "date" to System.currentTimeMillis(),
                        "wasConfirmed" to isConfirmed,
                        "duration" to duration
                    )
                ).await()

                Log.d(TAG, "Espacio desocupado: $reservationId")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marcando como completado", e)
            Result.failure(e)
        }
    }

    override suspend fun cancelReservation(reservationId: String, parkingSpotId: String): Result<Unit> {
        return try {
            firestore.collection(PARKING_COLLECTION).document(parkingSpotId)
                .update("occupiedSpaces", FieldValue.increment(-1)).await()

            firestore.collection(RESERVATIONS_COLLECTION).document(reservationId)
                .update(mapOf(
                    "isActive" to false,
                    "isCompleted" to false
                )).await()

            Log.d(TAG, "Reservación cancelada: $reservationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelando reservación", e)
            Result.failure(e)
        }
    }

    override suspend fun getReservationHistory(userId: String): Result<List<ReservationHistory>> {
        return try {
            val snapshot = firestore.collection(HISTORY_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val history = snapshot.documents
                .mapNotNull { doc ->
                    try {
                        ReservationHistory(
                            id = doc.id,
                            userId = userId,
                            basementNumber = (doc.getLong("basementNumber") ?: 0L).toInt(),
                            date = doc.getLong("date") ?: 0L,
                            wasConfirmed = doc.getBoolean("wasConfirmed") ?: false,
                            duration = doc.getLong("duration") ?: 0L
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapeando historial", e)
                        null
                    }
                }
                .sortedByDescending { it.date }

            Log.d(TAG, "Historial obtenido: ${history.size} registros")
            Result.success(history)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo historial", e)
            Result.failure(e)
        }
    }
}