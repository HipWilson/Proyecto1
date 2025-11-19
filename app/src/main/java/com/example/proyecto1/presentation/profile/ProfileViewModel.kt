package com.example.proyecto1.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.domain.repository.FirebaseAuthRepository
import com.example.proyecto1.domain.usecase.GetReservationHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getReservationHistoryUseCase: GetReservationHistoryUseCase = GetReservationHistoryUseCase(),
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Primero obtener el usuario actual
                authRepository.getCurrentUser()
                    .onSuccess { user ->
                        if (user != null) {
                            _state.update { state ->
                                state.copy(
                                    user = user,
                                    isLoading = false
                                )
                            }

                            // Luego obtener el historial desde Firebase
                            getReservationHistoryUseCase(user.id)
                                .onSuccess { history ->
                                    _state.update {
                                        it.copy(
                                            history = history,
                                            errorMessage = null,
                                            isLoading = false
                                        )
                                    }
                                    Log.d(TAG, "Historial cargado: ${history.size} registros")
                                }
                                .onFailure { exception ->
                                    _state.update {
                                        it.copy(
                                            errorMessage = exception.message ?: "Error al cargar historial",
                                            isLoading = false
                                        )
                                    }
                                    Log.e(TAG, "Error obteniendo historial", exception)
                                }
                        }
                    }
                    .onFailure { exception ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = exception.message ?: "Error al cargar perfil"
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
                Log.e(TAG, "Error en loadHistory", e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun retry() {
        loadHistory()
    }
}