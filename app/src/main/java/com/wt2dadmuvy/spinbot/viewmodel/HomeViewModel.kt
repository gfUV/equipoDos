package com.wt2dadmuvy.spinbot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wt2dadmuvy.spinbot.database.AppDatabase
import com.wt2dadmuvy.spinbot.repository.ChallengeRepository
import com.wt2dadmuvy.spinbot.repository.PokemonRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Estados del juego - HU 11
    enum class EstadoJuego { INACTIVO, GIRANDO, CUENTA_REGRESIVA, ESPERANDO_RETO }

    data class RandomChallengeDialogData(
        val challengeDescription: String,
        val pokemonImageUrl: String?
    )

    private val challengeRepository: ChallengeRepository
    private val pokemonRepository = PokemonRepository()

    private val _estadoJuego = MutableLiveData(EstadoJuego.INACTIVO)
    val estadoJuego: LiveData<EstadoJuego> = _estadoJuego

    private val _countdown = MutableLiveData(3)
    val countdown: LiveData<Int> = _countdown

    // Grados que rotará la botella en el turno actual
    private val _deltaGiro = MutableLiveData(0f)
    val deltaGiro: LiveData<Float> = _deltaGiro

    private val _randomChallengeDialogData = MutableLiveData<RandomChallengeDialogData?>()
    val randomChallengeDialogData: LiveData<RandomChallengeDialogData?> = _randomChallengeDialogData

    private var countdownJob: Job? = null
    private var spinJob: Job? = null
    private var randomChallengeJob: Job? = null

    init {
        val dao = AppDatabase.getDatabase(application).challengeDao()
        challengeRepository = ChallengeRepository(dao)
    }

    companion object {
        // Debe coincidir con la duración de la animación en HomeFragment
        const val DURACION_GIRO_MS = 6000L
    }

    // Countdown en loop 3→0 mientras el juego está inactivo (HU 2)
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

    // Inicia el giro: calcula rotación aleatoria y avanza el estado hasta ESPERANDO_RETO
    fun iniciarGiro() {
        if (_estadoJuego.value == EstadoJuego.GIRANDO) return

        countdownJob?.cancel()

        // Entre 3 y 5 vueltas + ángulo aleatorio para que pare en dirección distinta cada vez
        val vueltasCompletas = (3..5).random() * 360f
        val anguloAleatorio = (0..359).random().toFloat()
        _deltaGiro.value = vueltasCompletas + anguloAleatorio

        _estadoJuego.value = EstadoJuego.GIRANDO

        spinJob = viewModelScope.launch {
            delay(DURACION_GIRO_MS)

            _estadoJuego.value = EstadoJuego.CUENTA_REGRESIVA
            for (numero in 3 downTo 0) {
                _countdown.value = numero
                delay(1000)
            }

            _estadoJuego.value = EstadoJuego.ESPERANDO_RETO
        }
    }

    /**
     * HU 12 - Criterios 2 y 3.
     * Obtiene en segundo plano un reto aleatorio desde Room y una imagen aleatoria
     * de Pokémon desde la API indicada por el profesor.
     */
    fun cargarRetoAleatorioParaDialogo() {
        if (randomChallengeJob?.isActive == true) return

        randomChallengeJob = viewModelScope.launch {
            val challenge = challengeRepository.getRandomChallenge()
            val pokemonImageUrl = pokemonRepository.getRandomPokemonImageUrl()

            _randomChallengeDialogData.value = RandomChallengeDialogData(
                challengeDescription = challenge?.description
                    ?: "Agrega retos para que el juego pueda seleccionar uno aleatoriamente.",
                pokemonImageUrl = pokemonImageUrl
            )
        }
    }

    fun limpiarDatosDialogoReto() {
        _randomChallengeDialogData.value = null
    }

    // Vuelve al estado inicial y reactiva el countdown de preview
    fun reiniciarJuego() {
        spinJob?.cancel()
        randomChallengeJob?.cancel()
        _estadoJuego.value = EstadoJuego.INACTIVO
        startCountdownPreview()
    }

    override fun onCleared() {
        countdownJob?.cancel()
        spinJob?.cancel()
        randomChallengeJob?.cancel()
        super.onCleared()
    }
}
