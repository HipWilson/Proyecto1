package com.example.proyecto1.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.domain.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

class LoginViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    fun onLoginClick() {
        if (!validateInput()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Timeout de 30 segundos (Firebase puede ser lento)
                val result = withTimeoutOrNull(30.seconds) {
                    authRepository.login(_state.value.email, _state.value.password)
                }

                if (result != null) {
                    result.onSuccess { user ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isLoginSuccessful = true,
                                errorMessage = null
                            )
                        }
                    }
                        .onFailure { exception ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = exception.message ?: "Error al iniciar sesión"
                                )
                            }
                        }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Tiempo de conexión agotado. Verifica tu conexión a internet."
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error inesperado al iniciar sesión"
                    )
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val email = _state.value.email
        val password = _state.value.password

        var isValid = true

        if (email.isEmpty()) {
            _state.update { it.copy(emailError = "El correo no puede estar vacío") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(emailError = "Correo electrónico inválido") }
            isValid = false
        }

        if (password.isEmpty()) {
            _state.update { it.copy(passwordError = "La contraseña no puede estar vacía") }
            isValid = false
        }

        return isValid
    }
}