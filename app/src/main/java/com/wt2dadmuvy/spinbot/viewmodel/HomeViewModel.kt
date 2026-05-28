package com.wt2dadmuvy.spinbot.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla principal (HomeFragment).
 *
 * Maneja dos flujos:
 * 1. Cuenta regresiva de preview (loop 3→0 cuando el juego está inactivo) — HU 2
 * 2. Lógica del giro de botella — HU 11:
 *    INACTIVO → GIRANDO → CUENTA_REGRESIVA → ESPERANDO_RETO → INACTIVO
 */
class HomeViewModel : ViewModel() {

    // -------------------------------------------------------------------------
    // ESTADOS DEL JUEGO - HU 11
    // -------------------------------------------------------------------------

    /**
     * Estados posibles del juego durante una partida.
     *
     * INACTIVO       → Pantalla en reposo, preview countdown corriendo, botón visible
     * GIRANDO        → Botella animándose, botón oculto, audio de fondo pausado
     * CUENTA_REGRESIVA → Botella quieta en posición final, countdown 3→0 mostrándose
     * ESPERANDO_RETO → Countdown llegó a 0, se debe mostrar el diálogo de HU 12
     */
    enum class EstadoJuego { INACTIVO, GIRANDO, CUENTA_REGRESIVA, ESPERANDO_RETO }

    private val _estadoJuego = MutableLiveData(EstadoJuego.INACTIVO)
    val estadoJuego: LiveData<EstadoJuego> = _estadoJuego

    // -------------------------------------------------------------------------
    // COUNTDOWN - usado tanto para preview (HU 2) como para post-giro (HU 11)
    // -------------------------------------------------------------------------

    private val _countdown = MutableLiveData(3)
    val countdown: LiveData<Int> = _countdown

    // -------------------------------------------------------------------------
    // GIRO - HU 11 Criterio 1, 3 y 4
    // -------------------------------------------------------------------------

    /**
     * Cuántos grados debe rotar la botella en este turno.
     * El Fragment lee este valor al iniciar la animación.
     *
     * Se calcula como: (3 a 5 vueltas completas) + ángulo aleatorio de parada.
     * El ángulo aleatorio garantiza que cada giro pare en una dirección distinta (Criterio 3).
     */
    private val _deltaGiro = MutableLiveData(0f)
    val deltaGiro: LiveData<Float> = _deltaGiro

    // -------------------------------------------------------------------------
    // CONTROL INTERNO
    // -------------------------------------------------------------------------

    private var countdownJob: Job? = null
    private var spinJob: Job? = null

    companion object {
        /** Duración de la animación de giro en milisegundos. Debe coincidir con el Fragment. */
        const val DURACION_GIRO_MS = 4000L
    }

    // -------------------------------------------------------------------------
    // FUNCIONES PÚBLICAS
    // -------------------------------------------------------------------------

    /**
     * Inicia el countdown de preview que se muestra en el centro de la botella
     * cuando el juego está inactivo (HU 2 Criterio 5).
     * Corre en loop infinito 3→0 hasta que se llame a [iniciarGiro].
     * Evita iniciar si ya hay un countdown corriendo.
     */
    fun startCountdownPreview() {
        if (countdownJob?.isActive == true) return

        countdownJob = viewModelScope.launch {
            while (true) {
                for (number in 3 downTo 0) {
                    _countdown.value = number
                    delay(1000)
                }
                delay(700)
                _countdown.value = 3
                delay(700)
            }
        }
    }

    /**
     * Inicia una partida: gira la botella, luego muestra el countdown post-giro.
     *
     * Flujo completo:
     * 1. Cancela el preview countdown
     * 2. Calcula un delta de rotación aleatorio (Criterio 3)
     * 3. Cambia estado a GIRANDO → el Fragment inicia la animación
     * 4. Espera DURACION_GIRO_MS (el Fragment también usa este tiempo)
     * 5. Cambia a CUENTA_REGRESIVA → el Fragment muestra el countdown
     * 6. Countdown 3→2→1→0 (Criterio 5)
     * 7. Cambia a ESPERANDO_RETO → el Fragment muestra el diálogo HU 12 (TODO)
     *
     * HU 11 Criterio 4: el Fragment usa la rotación actual de la vista como punto
     * de partida, por lo que siempre arranca desde donde se detuvo.
     */
    fun iniciarGiro() {
        // Evitar iniciar si ya está girando
        if (_estadoJuego.value == EstadoJuego.GIRANDO) return

        // Detener el preview countdown para que no interfiera
        countdownJob?.cancel()

        // HU 11 Criterio 3: calcular rotación aleatoria
        // Entre 3 y 5 vueltas completas + ángulo aleatorio de parada (0-359°)
        val vueltasCompletas = (3..5).random() * 360f
        val anguloAleatorio = (0..359).random().toFloat()
        _deltaGiro.value = vueltasCompletas + anguloAleatorio

        // Cambiar estado → el Fragment detecta esto y arranca la animación
        _estadoJuego.value = EstadoJuego.GIRANDO

        spinJob = viewModelScope.launch {
            // Esperar a que termine la animación de giro
            delay(DURACION_GIRO_MS)

            // HU 11 Criterio 5: mostrar countdown 3→0 cuando la botella se detiene
            _estadoJuego.value = EstadoJuego.CUENTA_REGRESIVA
            for (numero in 3 downTo 0) {
                _countdown.value = numero
                delay(1000)
            }

            // HU 11 Criterio 6: avisar que es momento de mostrar el diálogo HU 12
            _estadoJuego.value = EstadoJuego.ESPERANDO_RETO
        }
    }

    /**
     * Reinicia el juego al estado inactivo.
     * Se llama cuando el jugador cierra el diálogo de HU 12 (botón "Cerrar").
     * Reactiva el preview countdown de la pantalla principal.
     */
    fun reiniciarJuego() {
        spinJob?.cancel()
        _estadoJuego.value = EstadoJuego.INACTIVO
        startCountdownPreview()
    }

    override fun onCleared() {
        countdownJob?.cancel()
        spinJob?.cancel()
        super.onCleared()
    }
}
