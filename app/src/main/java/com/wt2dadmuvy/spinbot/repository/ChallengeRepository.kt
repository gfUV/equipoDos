package com.wt2dadmuvy.spinbot.repository

import androidx.lifecycle.LiveData
import com.wt2dadmuvy.spinbot.model.Challenge
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz unificada para el repositorio de retos.
 * Define operaciones tanto para la persistencia local (Room) como para la remota (Firestore).
 */
interface ChallengeRepository {

    // --- Operaciones Locales (Room) ---

    val allChallenges: LiveData<List<Challenge>>

    suspend fun insertLocal(challenge: Challenge)

    suspend fun updateLocal(challenge: Challenge)

    suspend fun deleteLocal(challenge: Challenge)

    suspend fun getRandomChallenge(): Challenge?


    // --- Operaciones Remotas (Firestore) ---

    fun getRemoteChallenges(): Flow<Result<List<Challenge>>>

    /**
     * Agrega un reto a Firestore y retorna el objeto con el ID generado por Firestore.
     */
    fun addRemoteChallenge(challenge: Challenge): Flow<Result<Challenge>>

    fun updateRemoteChallenge(challenge: Challenge): Flow<Result<Unit>>

    fun deleteRemoteChallenge(challenge: Challenge): Flow<Result<Unit>>
}
