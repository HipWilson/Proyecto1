package com.example.proyecto1.presentation.login

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
)