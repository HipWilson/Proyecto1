package com.example.proyecto1.presentation.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.domain.model.Reservation
import com.example.proyecto1.domain.usecase.CancelReservationUseCase
import com.example.proyecto1.domain.usecase.ConfirmArrivalUseCase
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
    val errorMessage: String? = null
)

class ReservationViewModel(
    private val reserveParkingSpotUseCase: ReserveParkingSpotUseCase = ReserveParkingSpotUseCase(),
    private val confirmArrivalUseCase: ConfirmArrivalUseCase = ConfirmArrivalUseCase(),
    private val cancelReservationUseCase: CancelReservationUseCase = CancelReservationUseCase()
) : ViewModel() {

    private val _state = MutableStateFlow(ReservationState())
    val state: StateFlow<ReservationState> = _state.asStateFlow()

    fun createReservation(parkingId: String, basementNumber: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            reserveParkingSpotUseCase(parkingId, basementNumber)
                .onSuccess { reservation ->
                    _state.update {
                        it.copy(
                            reservation = reservation,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    startTimer()
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al crear reservación"
                        )
                    }
                }
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_state.value.remainingSeconds > 0 && !_state.value.isConfirmed && !_state.value.isCancelled) {
                delay(1000)
                _state.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
        }
    }

    fun confirmArrival() {
        val reservationId = _state.value.reservation?.id ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            confirmArrivalUseCase(reservationId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isConfirmed = true,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al confirmar llegada"
                        )
                    }
                }
        }
    }

    fun cancelReservation() {
        val reservationId = _state.value.reservation?.id ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            cancelReservationUseCase(reservationId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isCancelled = true,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al cancelar reservación"
                        )
                    }
                }
        }
    }
}