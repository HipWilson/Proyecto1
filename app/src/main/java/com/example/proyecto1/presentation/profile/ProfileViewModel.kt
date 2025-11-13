package com.example.proyecto1.presentation.profile

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

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

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

                        // Luego obtener el historial
                        getReservationHistoryUseCase(user.id)
                            .onSuccess { history ->
                                _state.update {
                                    it.copy(
                                        history = history,
                                        errorMessage = null
                                    )
                                }
                            }
                            .onFailure { exception ->
                                _state.update {
                                    it.copy(
                                        errorMessage = exception.message ?: "Error al cargar historial"
                                    )
                                }
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