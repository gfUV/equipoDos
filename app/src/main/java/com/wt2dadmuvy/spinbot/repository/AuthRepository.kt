package com.wt2dadmuvy.spinbot.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para el repositorio de autenticación.
 * Define las operaciones básicas de registro e inicio de sesión.
 */
interface AuthRepository {

    /**
     * Registra un nuevo usuario con correo y contraseña.
     * @return Un Flow que emite un Result exitoso o fallido.
     */
    fun registerUser(email: String, pass: String): Flow<Result<Unit>>

    /**
     * Inicia sesión con un usuario existente.
     * @return Un Flow que emite un Result exitoso o fallido.
     */
    fun loginUser(email: String, pass: String): Flow<Result<Unit>>
}
