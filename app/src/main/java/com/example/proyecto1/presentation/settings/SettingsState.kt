package com.example.proyecto1.presentation.settings

import com.example.proyecto1.ui.theme.Language

data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val isDarkTheme: Boolean = false,
    val currentLanguage: Language = Language.SPANISH
)