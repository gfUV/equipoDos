package com.wt2dadmuvy.spinbot.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel de la HU 2.0 - Ventana Login y Registro.
 *
 * En esta primera entrega de la HU 2 solo se implementan los criterios 1 al 5.
 * Por eso este ViewModel se concentra en la validación en tiempo real del campo
 * Password, manteniendo la lógica fuera del Fragment según el patrón MVVM.
 */
class LoginViewModel : ViewModel() {

    private val _passwordError = MutableLiveData<String?>(null)
    val passwordError: LiveData<String?> = _passwordError

    /**
     * HU 2 - Criterio 5:
     * La contraseña debe tener mínimo 6 números y máximo 10.
     * El máximo se controla desde el XML con maxLength=10; aquí se valida
     * en tiempo real el mínimo solicitado por el profesor.
     */
    fun onPasswordChanged(password: String) {
        _passwordError.value = when {
            password.isNotEmpty() && password.length < MIN_PASSWORD_LENGTH -> MIN_PASSWORD_MESSAGE
            else -> null
        }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        const val MIN_PASSWORD_MESSAGE = "Mínimo 6 dígitos"
    }
}
