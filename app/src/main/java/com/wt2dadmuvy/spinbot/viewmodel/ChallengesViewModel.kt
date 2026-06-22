package com.wt2dadmuvy.spinbot.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wt2dadmuvy.spinbot.model.Challenge
import com.wt2dadmuvy.spinbot.repository.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la pantalla de retos usando el repositorio unificado y Hilt.
 */
@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val repository: ChallengeRepository
) : ViewModel() {

    // Lista observada por ChallengesFragment; se actualiza automáticamente cuando cambia la BD local
    val allChallenges: LiveData<List<Challenge>> = repository.allChallenges

    /**
     * Inserta un reto.
     * Para garantizar la integridad, primero lo subimos a Firestore para obtener un ID único
     * y luego lo guardamos localmente con ese mismo ID.
     */
    fun insert(challenge: Challenge) = viewModelScope.launch {
        repository.addRemoteChallenge(challenge).collect { result ->
            result.onSuccess { challengeWithId ->
                repository.insertLocal(challengeWithId)
            }
        }
    }

    /**
     * Actualiza un reto en ambos almacenamientos.
     */
    fun update(challenge: Challenge) = viewModelScope.launch {
        repository.updateLocal(challenge)
        repository.updateRemoteChallenge(challenge).collect { }
    }

    /**
     * Elimina un reto de ambos almacenamientos.
     */
    fun delete(challenge: Challenge) = viewModelScope.launch {
        repository.deleteLocal(challenge)
        repository.deleteRemoteChallenge(challenge).collect { }
    }
}
