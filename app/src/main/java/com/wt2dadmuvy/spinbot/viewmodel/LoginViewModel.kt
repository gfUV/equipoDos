package com.wt2dadmuvy.spinbot.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel de la HU 2.0 - Ventana Login y Registro.
 *
 * Mantiene la lógica de validación y estado de la pantalla fuera del Fragment,
 * siguiendo el patrón MVVM recomendado por el profesor.
 *
 * En esta entrega se implementan los criterios solicitados:
 * - C1 a C5 ya estaban implementados.
 * - C6: alternar visibilidad de Password con el ícono del ojo.
 * - C7/C8: estado del botón Login según campos llenos.
 * - C11/C12: estado del texto-botón Registrarse según campos llenos.
 * - C15: la vista usa ondas inferiores dibujadas con XML vectorial.
 */
class LoginViewModel : ViewModel() {

    private val _passwordError = MutableLiveData<String?>(null)
    val passwordError: LiveData<String?> = _passwordError

    private val _actionsEnabled = MutableLiveData(false)
    val actionsEnabled: LiveData<Boolean> = _actionsEnabled

    private val _passwordVisible = MutableLiveData(false)
    val passwordVisible: LiveData<Boolean> = _passwordVisible

    private var currentEmail: String = ""
    private var currentPassword: String = ""

    fun onEmailChanged(email: String) {
        currentEmail = email
        updateActionsState()
    }

    /**
     * HU 2 - Criterio 5:
     * La contraseña debe tener mínimo 6 números y máximo 10.
     * El máximo y el ingreso solo numérico se controlan desde XML.
     * Aquí se valida en tiempo real el mínimo solicitado.
     */
    fun onPasswordChanged(password: String) {
        currentPassword = password

        _passwordError.value = when {
            password.isNotEmpty() && password.length < MIN_PASSWORD_LENGTH -> MIN_PASSWORD_MESSAGE
            else -> null
        }

        updateActionsState()
    }

    /**
     * HU 2 - Criterios 7, 8, 11 y 12:
     * Login y Registrarse solo se habilitan cuando Email y Password tienen datos.
     */
    private fun updateActionsState() {
        _actionsEnabled.value = currentEmail.isNotBlank() && currentPassword.isNotBlank()
    }

    /**
     * HU 2 - Criterio 6:
     * Alterna entre password oculto y visible usando el ícono del ojo.
     */
    fun togglePasswordVisibility() {
        _passwordVisible.value = !(_passwordVisible.value ?: false)
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        const val MIN_PASSWORD_MESSAGE = "Mínimo 6 dígitos"
    }
}
