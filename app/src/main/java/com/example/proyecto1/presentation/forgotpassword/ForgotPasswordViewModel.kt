package com.example.proyecto1.presentation.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.domain.usecase.ForgotPasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null
)

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase = ForgotPasswordUseCase()
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onSendClick() {
        if (!validateInput()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            forgotPasswordUseCase(_state.value.email)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSuccessful = true,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al enviar correo"
                        )
                    }
                }
        }
    }

    private fun validateInput(): Boolean {
        val email = _state.value.email

        if (email.isEmpty()) {
            _state.update { it.copy(emailError = "El correo no puede estar vacío") }
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(emailError = "Correo electrónico inválido") }
            return false
        }

        return true
    }
}