package com.example.proyecto1.presentation.settings

import androidx.lifecycle.ViewModel
import com.example.proyecto1.ui.theme.ThemeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        _state.update { it.copy(isDarkTheme = ThemeState.getCurrentTheme()) }
    }

    fun onNotificationsToggle(enabled: Boolean) {
        _state.update { it.copy(notificationsEnabled = enabled) }
    }

    fun onThemeToggle(isDark: Boolean) {
        _state.update { it.copy(isDarkTheme = isDark) }
        ThemeState.setDarkTheme(isDark)
    }
}