package com.example.proyecto1.presentation.register

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

class RegisterViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onNameChange(name: String) {
        _state.update { it.copy(name = name, nameError = null, errorMessage = null) }
    }

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update {
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = null,
                errorMessage = null
            )
        }
    }

    fun onRegisterClick() {
        if (!validateInput()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Timeout de 30 segundos (Firebase puede ser lento)
                val result = withTimeoutOrNull(30.seconds) {
                    authRepository.register(
                        _state.value.name,
                        _state.value.email,
                        _state.value.password
                    )
                }

                if (result != null) {
                    result.onSuccess { user ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRegisterSuccessful = true,
                                errorMessage = null
                            )
                        }
                    }
                        .onFailure { exception ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = exception.message ?: "Error al registrarse"
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
                        errorMessage = e.message ?: "Error inesperado al registrarse"
                    )
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val name = _state.value.name
        val email = _state.value.email
        val password = _state.value.password
        val confirmPassword = _state.value.confirmPassword

        var isValid = true

        if (name.isEmpty()) {
            _state.update { it.copy(nameError = "El nombre no puede estar vacío") }
            isValid = false
        }

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
        } else if (password.length < 6) {
            _state.update {
                it.copy(passwordError = "La contraseña debe tener al menos 6 caracteres")
            }
            isValid = false
        }

        if (confirmPassword != password) {
            _state.update { it.copy(confirmPasswordError = "Las contraseñas no coinciden") }
            isValid = false
        }

        return isValid
    }
}