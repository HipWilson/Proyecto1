package com.example.proyecto1.domain.repository

import android.util.Log
import com.example.proyecto1.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

interface IAuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun resetPassword(email: String): Result<Unit>
    fun isUserLoggedIn(): Boolean
}

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : IAuthRepository {

    companion object {
        private const val TAG = "FirebaseAuthRepository"

        init {
            Log.d(TAG, "FirebaseAuthRepository inicializado")
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            Log.d(TAG, "Iniciando login con: $email")

            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            Log.d(TAG, "Login exitoso: ${firebaseUser?.uid}")

            if (firebaseUser != null) {
                Result.success(
                    User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "Usuario",
                        email = firebaseUser.email ?: ""
                    )
                )
            } else {
                Log.e(TAG, "Usuario es nulo después del login")
                Result.failure(Exception("No se pudo obtener información del usuario"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login: ${e.message}", e)
            Result.failure(parseFirebaseException(e))
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            Log.d(TAG, "Iniciando registro con: $email")

            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            Log.d(TAG, "Registro exitoso, actualizando perfil: ${firebaseUser?.uid}")

            if (firebaseUser != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                firebaseUser.updateProfile(profileUpdates).await()

                Log.d(TAG, "Perfil actualizado correctamente")

                Result.success(
                    User(
                        id = firebaseUser.uid,
                        name = name,
                        email = email
                    )
                )
            } else {
                Log.e(TAG, "Usuario es nulo después del registro")
                Result.failure(Exception("Error al crear usuario"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en registro: ${e.message}", e)
            Result.failure(parseFirebaseException(e))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            Log.d(TAG, "Cerrando sesión")
            firebaseAuth.signOut()
            Log.d(TAG, "Sesión cerrada exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val firebaseUser = firebaseAuth.currentUser

            if (firebaseUser != null) {
                Log.d(TAG, "Usuario actual: ${firebaseUser.uid}")
                Result.success(
                    User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "Usuario",
                        email = firebaseUser.email ?: ""
                    )
                )
            } else {
                Log.d(TAG, "No hay usuario autenticado")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener usuario actual: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            Log.d(TAG, "Enviando correo de recuperación a: $email")
            firebaseAuth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Correo enviado exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar correo: ${e.message}", e)
            Result.failure(parseFirebaseException(e))
        }
    }

    override fun isUserLoggedIn(): Boolean {
        val isLogged = firebaseAuth.currentUser != null
        Log.d(TAG, "¿Usuario autenticado? $isLogged")
        return isLogged
    }

    private fun parseFirebaseException(exception: Exception): Exception {
        val errorMessage = exception.message?.toString() ?: "Error desconocido"
        Log.d(TAG, "Error original de Firebase: $errorMessage")

        val message = when {
            errorMessage.contains("The password is invalid", ignoreCase = true) ->
                "La contraseña es incorrecta"
            errorMessage.contains("There is no user record corresponding to this identifier", ignoreCase = true) ->
                "No existe cuenta con este correo"
            errorMessage.contains("The email address is badly formatted", ignoreCase = true) ->
                "Correo electrónico inválido"
            errorMessage.contains("The email address is already in use by another account", ignoreCase = true) ->
                "Este correo ya está registrado"
            errorMessage.contains("Password should be at least 6 characters", ignoreCase = true) ->
                "La contraseña debe tener al menos 6 caracteres"
            errorMessage.contains("Network error", ignoreCase = true) ->
                "Error de conexión a internet. Intenta de nuevo."
            errorMessage.contains("Too many unsuccessful login attempts", ignoreCase = true) ->
                "Demasiados intentos fallidos. Intenta más tarde."
            else -> errorMessage
        }
        Log.d(TAG, "Error parseado: $message")
        return Exception(message)
    }
}