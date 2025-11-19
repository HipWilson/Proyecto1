package com.example.proyecto1.presentation.reservation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.domain.model.Reservation
import com.example.proyecto1.domain.repository.FirebaseAuthRepository
import com.example.proyecto1.domain.repository.FirebaseParkingRepository
import com.example.proyecto1.domain.usecase.CancelReservationUseCase
import com.example.proyecto1.domain.usecase.ConfirmArrivalUseCase
import com.example.proyecto1.domain.usecase.MarkAsCompletedUseCase
import com.example.proyecto1.domain.usecase.ReserveParkingSpotUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReservationState(
    val reservation: Reservation? = null,
    val remainingSeconds: Int = 300, // 5 minutos
    val isLoading: Boolean = false,
    val isConfirmed: Boolean = false,
    val isCancelled: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
)

class ReservationViewModel(
    private val reserveParkingSpotUseCase: ReserveParkingSpotUseCase = ReserveParkingSpotUseCase(),
    private val confirmArrivalUseCase: ConfirmArrivalUseCase = ConfirmArrivalUseCase(),
    private val cancelReservationUseCase: CancelReservationUseCase = CancelReservationUseCase(),
    private val markAsCompletedUseCase: MarkAsCompletedUseCase = MarkAsCompletedUseCase(),
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ReservationState())
    val state: StateFlow<ReservationState> = _state.asStateFlow()

    companion object {
        private const val TAG = "ReservationViewModel"
    }

    fun createReservation(parkingId: String, basementNumber: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Obtener el usuario actual
                val userResult = authRepository.getCurrentUser()
                userResult.onSuccess { user ->
                    if (user != null) {
                        viewModelScope.launch {
                            reserveParkingSpotUseCase(parkingId, basementNumber, user.id)
                                .onSuccess { reservation ->
                                    _state.update {
                                        it.copy(
                                            reservation = reservation,
                                            isLoading = false,
                                            errorMessage = null
                                        )
                                    }
                                    startTimer()
                                    Log.d(TAG, "Reservación creada: ${reservation.id}")
                                }
                                .onFailure { exception ->
                                    _state.update {
                                        it.copy(
                                            isLoading = false,
                                            errorMessage = exception.message ?: "Error al crear reservación"
                                        )
                                    }
                                    Log.e(TAG, "Error creando reservación", exception)
                                }
                        }
                    }
                }.onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo obtener información del usuario"
                        )
                    }
                    Log.e(TAG, "Error obteniendo usuario", exception)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error inesperado"
                    )
                }
                Log.e(TAG, "Error en createReservation", e)
            }
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_state.value.remainingSeconds > 0 &&
                !_state.value.isConfirmed &&
                !_state.value.isCancelled &&
                !_state.value.isCompleted) {
                delay(1000)
                _state.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
        }
    }

    fun confirmArrival() {
        val reservation = _state.value.reservation ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                confirmArrivalUseCase(reservation.id, reservation.parkingSpotId)
                    .onSuccess {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isConfirmed = true,
                                errorMessage = null
                            )
                        }
                        Log.d(TAG, "Llegada confirmada")
                    }
                    .onFailure { exception ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = exception.message ?: "Error al confirmar llegada"
                            )
                        }
                        Log.e(TAG, "Error confirmando llegada", exception)
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error inesperado"
                    )
                }
                Log.e(TAG, "Error en confirmArrival", e)
            }
        }
    }

    fun cancelReservation() {
        val reservation = _state.value.reservation ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                cancelReservationUseCase(reservation.id, reservation.parkingSpotId)
                    .onSuccess {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isCancelled = true,
                                errorMessage = null
                            )
                        }
                        Log.d(TAG, "Reservación cancelada")
                    }
                    .onFailure { exception ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = exception.message ?: "Error al cancelar reservación"
                            )
                        }
                        Log.e(TAG, "Error cancelando reservación", exception)
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error inesperado"
                    )
                }
                Log.e(TAG, "Error en cancelReservation", e)
            }
        }
    }

    fun markAsCompleted() {
        val reservation = _state.value.reservation ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                markAsCompletedUseCase(reservation.id, reservation.parkingSpotId)
                    .onSuccess {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isCompleted = true,
                                errorMessage = null
                            )
                        }
                        Log.d(TAG, "Espacio marcado como desocupado")
                    }
                    .onFailure { exception ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = exception.message ?: "Error al desocupar espacio"
                            )
                        }
                        Log.e(TAG, "Error marcando como completado", exception)
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error inesperado"
                    )
                }
                Log.e(TAG, "Error en markAsCompleted", e)
            }
        }
    }
}