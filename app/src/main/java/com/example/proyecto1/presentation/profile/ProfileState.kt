package com.example.proyecto1.presentation.profile

import com.example.proyecto1.domain.model.ReservationHistory
import com.example.proyecto1.domain.model.User

data class ProfileState(
    val user: User = User(
        id = "user_1",
        name = "Usuario Demo",
        email = "usuario@uvg.edu.gt"
    ),
    val history: List<ReservationHistory> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)