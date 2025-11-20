package com.example.proyecto1.presentation.settings

data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val isDarkTheme: Boolean = false
)