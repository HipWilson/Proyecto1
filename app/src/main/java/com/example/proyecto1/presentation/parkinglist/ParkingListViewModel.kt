package com.example.proyecto1.presentation.parkinglist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.domain.repository.FirebaseAuthRepository
import com.example.proyecto1.domain.usecase.GetActiveReservationUseCase
import com.example.proyecto1.domain.usecase.GetParkingSpotsUseCase
import com.example.proyecto1.domain.usecase.MarkAsCompletedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ParkingListViewModel(
    private val getParkingSpotsUseCase: GetParkingSpotsUseCase = GetParkingSpotsUseCase(),
    private val getActiveReservationUseCase: GetActiveReservationUseCase = GetActiveReservationUseCase(),
    private val markAsCompletedUseCase: MarkAsCompletedUseCase = MarkAsCompletedUseCase(),
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ParkingListState())
    val state: StateFlow<ParkingListState> = _state.asStateFlow()

    companion object {
        private const val TAG = "ParkingListViewModel"
    }

    init {
        loadParkingSpots()
        checkActiveReservation()
    }

    private fun loadParkingSpots() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                getParkingSpotsUseCase().collect { spots ->
                    _state.update {
                        it.copy(
                            parkingSpots = spots,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando sótanos: ${e.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al cargar los sótanos"
                    )
                }
            }
        }
    }

    private fun checkActiveReservation() {
        viewModelScope.launch {
            try {
                authRepository.getCurrentUser()
                    .onSuccess { user ->
                        if (user != null) {
                            getActiveReservationUseCase(user.id)
                                .onSuccess { activeReservation ->
                                    if (activeReservation != null) {
                                        Log.d(TAG, "Usuario tiene reserva activa: ${activeReservation.id}")
                                        _state.update {
                                            it.copy(
                                                hasActiveReservation = true,
                                                activeReservationId = activeReservation.id,
                                                activeReservationBasement = activeReservation.basementNumber
                                            )
                                        }
                                    } else {
                                        Log.d(TAG, "Usuario no tiene reservas activas")
                                        _state.update {
                                            it.copy(
                                                hasActiveReservation = false,
                                                activeReservationId = null,
                                                activeReservationBasement = null
                                            )
                                        }
                                    }
                                }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error verificando reserva activa: ${e.message}")
            }
        }
    }

    fun completeActiveReservation(onComplete: () -> Unit) {
        val reservationId = _state.value.activeReservationId ?: return

        viewModelScope.launch {
            try {
                authRepository.getCurrentUser()
                    .onSuccess { user ->
                        if (user != null) {
                            getActiveReservationUseCase(user.id)
                                .onSuccess { reservation ->
                                    if (reservation != null) {
                                        markAsCompletedUseCase(
                                            reservationId,
                                            reservation.parkingSpotId
                                        )
                                            .onSuccess {
                                                Log.d(TAG, "Reserva marcada como completada")
                                                _state.update {
                                                    it.copy(
                                                        hasActiveReservation = false,
                                                        activeReservationId = null,
                                                        activeReservationBasement = null
                                                    )
                                                }
                                                onComplete()
                                            }
                                            .onFailure { exception ->
                                                Log.e(TAG, "Error marcando como completado", exception)
                                            }
                                    }
                                }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error en completeActiveReservation: ${e.message}")
            }
        }
    }

    fun onTabSelected(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    fun retry() {
        loadParkingSpots()
        checkActiveReservation()
    }

    fun canMakeReservation(): Boolean {
        return !_state.value.hasActiveReservation
    }

    fun getActiveReservationMessage(): String {
        return if (_state.value.hasActiveReservation) {
            "Ya tienes un apartado activo en el Sótano ${_state.value.activeReservationBasement}. Debes marcarlo como desocupado antes de hacer otro apartado."
        } else {
            ""
        }
    }
}