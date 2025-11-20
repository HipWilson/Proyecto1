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
                authRepository.getCurrentUser()
                    .onSuccess { user ->
                        if (user != null) {
                            Log.d(TAG, "Usuario actual obtenido: ${user.id}")
                            _state.update { state ->
                                state.copy(user = user)
                            }

                            getReservationHistoryUseCase(user.id)
                                .onSuccess { history ->
                                    Log.d(TAG, "Historial obtenido: ${history.size} registros")
                                    history.forEach { h ->
                                        Log.d(TAG, "Historial item: Sótano ${h.basementNumber}, Confirmado: ${h.wasConfirmed}, Duración: ${h.duration}")
                                    }
                                    _state.update {
                                        it.copy(
                                            history = history,
                                            errorMessage = null,
                                            isLoading = false
                                        )
                                    }
                                }
                                .onFailure { exception ->
                                    Log.e(TAG, "Error obteniendo historial: ${exception.message}", exception)
                                    _state.update {
                                        it.copy(
                                            errorMessage = exception.message ?: "Error al cargar historial",
                                            isLoading = false,
                                            history = emptyList()
                                        )
                                    }
                                }
                        } else {
                            Log.d(TAG, "No hay usuario autenticado")
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "No hay usuario autenticado"
                                )
                            }
                        }
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "Error obteniendo usuario: ${exception.message}", exception)
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = exception.message ?: "Error al cargar perfil"
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado en loadHistory: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error inesperado"
                    )
                }
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