package com.example.proyecto1.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

enum class Language(val code: String, val displayName: String) {
    SPANISH("es", "Español"),
    ENGLISH("en", "English")
}

object LanguageState {
    private val _currentLanguage = MutableStateFlow(Language.SPANISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    fun setLanguage(language: Language) {
        _currentLanguage.value = language
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == Language.SPANISH) {
            Language.ENGLISH
        } else {
            Language.SPANISH
        }
    }

    fun getCurrentLanguage(): Language = _currentLanguage.value
}

// Diccionario de strings para ambos idiomas
object StringResources {
    private val strings = mapOf(
        Language.SPANISH to mapOf(
            // App
            "app_name" to "FindMySpot UVG",

            // Login Screen
            "login_title" to "Iniciar Sesión",
            "login_email" to "Correo electrónico",
            "login_password" to "Contraseña",
            "login_button" to "Iniciar Sesión",
            "login_forgot_password" to "¿Olvidaste tu contraseña?",
            "login_no_account" to "¿No tienes cuenta?",
            "login_register" to "Regístrate",

            // Register Screen
            "register_title" to "Crear Cuenta",
            "register_name" to "Nombre completo",
            "register_email" to "Correo electrónico",
            "register_password" to "Contraseña",
            "register_confirm_password" to "Confirmar contraseña",
            "register_button" to "Registrarse",
            "register_have_account" to "¿Ya tienes cuenta?",
            "register_login" to "Inicia sesión",

            // Forgot Password Screen
            "forgot_password_title" to "Recuperar Contraseña",
            "forgot_password_description" to "Ingresa tu correo electrónico y te enviaremos instrucciones para recuperar tu contraseña",
            "forgot_password_email" to "Correo electrónico",
            "forgot_password_button" to "Enviar",
            "forgot_password_back_to_login" to "Volver a iniciar sesión",
            "forgot_password_success" to "¡Correo enviado exitosamente!",

            // Parking List Screen
            "parking_list_title" to "Sótanos Disponibles",
            "parking_list_available" to "Disponible",
            "parking_list_few_spots" to "Pocos espacios",
            "parking_list_full" to "Lleno",
            "parking_list_reserve" to "Apartar",
            "parking_list_basement" to "Sótano %d",

            // Reservation Screen
            "reservation_title" to "Apartado Simbólico",
            "reservation_description" to "Has apartado un espacio en:",
            "reservation_time_remaining" to "Tiempo restante:",
            "reservation_confirm_arrival" to "Confirmar Llegada",
            "reservation_cancel" to "Cancelar Apartado",
            "reservation_expired" to "Tu apartado ha expirado",

            // Profile Screen
            "profile_title" to "Mi Perfil",
            "profile_history" to "Historial de Apartados",
            "profile_logout" to "Cerrar Sesión",
            "profile_no_history" to "No tienes historial de apartados",

            // Settings Screen
            "settings_title" to "Ajustes",
            "settings_notifications" to "Notificaciones",
            "settings_notifications_description" to "Recibir alertas de espacios llenos",
            "settings_theme" to "Tema",
            "settings_language" to "Idioma",

            // Common
            "common_cancel" to "Cancelar",
            "common_accept" to "Aceptar",
            "common_back" to "Atrás",
        ),
        Language.ENGLISH to mapOf(
            // App
            "app_name" to "FindMySpot UVG",

            // Login Screen
            "login_title" to "Sign In",
            "login_email" to "Email",
            "login_password" to "Password",
            "login_button" to "Sign In",
            "login_forgot_password" to "Forgot your password?",
            "login_no_account" to "Don't have an account?",
            "login_register" to "Sign Up",

            // Register Screen
            "register_title" to "Create Account",
            "register_name" to "Full Name",
            "register_email" to "Email",
            "register_password" to "Password",
            "register_confirm_password" to "Confirm Password",
            "register_button" to "Sign Up",
            "register_have_account" to "Already have an account?",
            "register_login" to "Sign In",

            // Forgot Password Screen
            "forgot_password_title" to "Recover Password",
            "forgot_password_description" to "Enter your email and we'll send you instructions to recover your password",
            "forgot_password_email" to "Email",
            "forgot_password_button" to "Send",
            "forgot_password_back_to_login" to "Back to Sign In",
            "forgot_password_success" to "Email sent successfully!",

            // Parking List Screen
            "parking_list_title" to "Available Parking",
            "parking_list_available" to "Available",
            "parking_list_few_spots" to "Few Spaces",
            "parking_list_full" to "Full",
            "parking_list_reserve" to "Reserve",
            "parking_list_basement" to "Basement %d",

            // Reservation Screen
            "reservation_title" to "Symbolic Reservation",
            "reservation_description" to "You've reserved a space in:",
            "reservation_time_remaining" to "Time Remaining:",
            "reservation_confirm_arrival" to "Confirm Arrival",
            "reservation_cancel" to "Cancel Reservation",
            "reservation_expired" to "Your reservation has expired",

            // Profile Screen
            "profile_title" to "My Profile",
            "profile_history" to "Reservation History",
            "profile_logout" to "Sign Out",
            "profile_no_history" to "You have no reservation history",

            // Settings Screen
            "settings_title" to "Settings",
            "settings_notifications" to "Notifications",
            "settings_notifications_description" to "Receive alerts when spaces are full",
            "settings_theme" to "Theme",
            "settings_language" to "Language",

            // Common
            "common_cancel" to "Cancel",
            "common_accept" to "Accept",
            "common_back" to "Back",
        )
    )

    fun getString(key: String): String {
        val language = LanguageState.getCurrentLanguage()
        return strings[language]?.get(key) ?: strings[Language.SPANISH]?.get(key) ?: key
    }

    fun getString(key: String, vararg args: Any): String {
        val template = getString(key)
        return try {
            template.format(*args)
        } catch (e: Exception) {
            template
        }
    }
}

// Composable helper para obtener strings del idioma actual
@Composable
fun rememberString(key: String): String {
    val currentLanguage = LanguageState.currentLanguage.collectAsState()
    return StringResources.getString(key)
}

@Composable
fun rememberString(key: String, vararg args: Any): String {
    val currentLanguage = LanguageState.currentLanguage.collectAsState()
    return StringResources.getString(key, *args)
}

