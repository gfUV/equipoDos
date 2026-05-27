package com.wt2dadmuvy.spinbot.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel compartido entre HomeFragment, ChallengesFragment (HU 6) e InstructionsFragment (HU 5).
 *
 * Su propósito es centralizar el estado del audio de fondo para que cualquier
 * Fragment pueda saber si el audio está encendido y solicitar pausarlo o reanudarlo
 * sin necesidad de comunicarse directamente entre sí.
 *
 * Se accede con: private val sharedAudioViewModel: SharedAudioViewModel by activityViewModels()
 */
class SharedAudioViewModel : ViewModel() {

    // Estado real del audio: true = encendido, false = apagado por el usuario
    private val _isAudioOn = MutableLiveData(true)
    val isAudioOn: LiveData<Boolean> = _isAudioOn

    // Solicitud de pausa temporal: true = pausar, false = reanudar
    // Lo usan HU 5 y HU 6 al entrar/salir de sus pantallas
    private val _pauseRequested = MutableLiveData(false)
    val pauseRequested: LiveData<Boolean> = _pauseRequested

    /**
     * Cambia el estado real del audio (accionado por el botón de audio en HomeFragment).
     * @param value true si el usuario encendió el audio, false si lo apagó.
     */
    fun setAudioOn(value: Boolean) {
        _isAudioOn.value = value
    }

    /**
     * Solicita pausar o reanudar el audio temporalmente.
     * Lo llaman HU 5 (Instrucciones) y HU 6 (Retos) al entrar y salir de sus fragments.
     * HomeFragment observa este valor y actúa sobre el MediaPlayer.
     *
     * @param pause true al entrar al fragment (pausar), false al salir (reanudar si estaba ON).
     */
    fun requestPause(pause: Boolean) {
        _pauseRequested.value = pause
    }
}
