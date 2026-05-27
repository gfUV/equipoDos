package com.wt2dadmuvy.spinbot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.wt2dadmuvy.spinbot.database.AppDatabase
import com.wt2dadmuvy.spinbot.model.Challenge
import com.wt2dadmuvy.spinbot.repository.ChallengeRepository
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla de Retos (HU 6).
 *
 * ¿Qué es un ViewModel?
 * Es la capa que conecta la pantalla (ChallengesFragment) con los datos (Repository).
 * Su ventaja principal: sobrevive a los cambios de configuración del dispositivo
 * (como rotar la pantalla). Si el Fragment se destruye y recrea, el ViewModel mantiene
 * los datos intactos sin volver a consultar la base de datos.
 *
 * ¿Por qué AndroidViewModel y no ViewModel?
 * Porque necesitamos el "Application context" para inicializar AppDatabase (Room).
 * AndroidViewModel recibe la Application automáticamente al crearse.
 * NUNCA se debe guardar un Activity o Fragment context dentro de un ViewModel —
 * causaría fugas de memoria (Memory Leaks).
 *
 * ¿Qué es viewModelScope?
 * Es un CoroutineScope que vive mientras el ViewModel esté activo.
 * Al destruirse el ViewModel, cancela automáticamente todas las corrutinas en curso.
 * Se usa para ejecutar operaciones asíncronas (insert, update, delete) sin bloquear la UI.
 */
class ChallengesViewModel(application: Application) : AndroidViewModel(application) {

    // Referencia al repositorio — toda comunicación con la BD pasa por aquí
    private val repository: ChallengeRepository

    /**
     * Lista de retos expuesta al Fragment como LiveData (solo lectura).
     * El Fragment observa este dato: cada vez que la BD cambia, la pantalla
     * se actualiza automáticamente.
     */
    val allChallenges: LiveData<List<Challenge>>

    /**
     * Bloque init: se ejecuta una sola vez cuando el ViewModel se crea.
     * Aquí se inicializa la cadena: AppDatabase → DAO → Repository → LiveData.
     */
    init {
        val dao = AppDatabase.getDatabase(application).challengeDao()
        repository = ChallengeRepository(dao)
        allChallenges = repository.allChallenges
    }

    /**
     * Inserta un nuevo reto en la base de datos.
     * viewModelScope.launch lanza la corrutina en hilo de fondo (definido en el Repository).
     * El Fragment llama esta función cuando el usuario guarda un reto en HU 7 (Jonatan).
     *
     * @param challenge El reto a guardar con su nombre y descripción.
     */
    fun insert(challenge: Challenge) {
        viewModelScope.launch {
            repository.insert(challenge)
        }
    }

    /**
     * Actualiza un reto existente en la base de datos.
     * El Fragment llamará esta función cuando Alexandra (HU 8) confirme la edición.
     *
     * @param challenge El reto con los datos ya modificados (mismo id, nueva descripción).
     */
    fun update(challenge: Challenge) {
        viewModelScope.launch {
            repository.update(challenge)
        }
    }

    /**
     * Elimina un reto de la base de datos.
     * El Fragment llamará esta función cuando German (HU 9) confirme la eliminación.
     *
     * @param challenge El reto que se desea eliminar (Room lo identifica por su id).
     */
    fun delete(challenge: Challenge) {
        viewModelScope.launch {
            repository.delete(challenge)
        }
    }
}
