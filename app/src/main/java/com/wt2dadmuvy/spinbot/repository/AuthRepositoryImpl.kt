package com.wt2dadmuvy.spinbot.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Implementación de [AuthRepository] utilizando Firebase Authentication.
 */
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override fun registerUser(email: String, pass: String): Flow<Result<Unit>> = callbackFlow {
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.success(Unit))
                } else {
                    val exception = task.exception ?: Exception("Error desconocido en el registro")
                    trySend(Result.failure(exception))
                }
                close()
            }
        awaitClose { /* No es necesario limpiar listeners de tareas únicas */ }
    }

    override fun loginUser(email: String, pass: String): Flow<Result<Unit>> = callbackFlow {
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.success(Unit))
                } else {
                    val exception = task.exception ?: Exception("Error desconocido al iniciar sesión")
                    trySend(Result.failure(exception))
                }
                close()
            }
        awaitClose { /* No es necesario limpiar listeners de tareas únicas */ }
    }

    override fun logoutUser() {
        firebaseAuth.signOut()
    }
}
