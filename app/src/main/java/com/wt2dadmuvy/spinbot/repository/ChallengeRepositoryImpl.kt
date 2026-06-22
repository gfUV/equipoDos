package com.wt2dadmuvy.spinbot.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wt2dadmuvy.spinbot.database.ChallengeDao
import com.wt2dadmuvy.spinbot.model.Challenge
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementación unificada de [ChallengeRepository].
 * Gestiona datos locales vía [ChallengeDao] (Room) y datos remotos vía [FirebaseFirestore].
 */
class ChallengeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val challengeDao: ChallengeDao
) : ChallengeRepository {

    // --- Implementación del Origen de la Verdad (Firestore) ---

    /**
     * Reemplazo de lectura local por remota:
     * allChallenges ahora observa Firestore en tiempo real.
     */
    override val allChallenges: LiveData<List<Challenge>> = callbackFlow {
        val collection = getChallengesCollection()
        if (collection == null) {
            trySend(emptyList<Challenge>())
        } else {
            val listener = collection.addSnapshotListener { snapshot, _ ->
                val challenges = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Challenge::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(challenges)
            }
            awaitClose { listener.remove() }
        }
    }.asLiveData()

    /**
     * Reemplazo de lectura aleatoria local por remota:
     * Obtiene los retos de Firestore y elige uno al azar.
     */
    override suspend fun getRandomChallenge(): Challenge? {
        val collection = getChallengesCollection() ?: return null
        return try {
            val snapshot = collection.get().await()
            val challenges = snapshot.documents.mapNotNull { it.toObject(Challenge::class.java)?.apply { id = it.id } }
            if (challenges.isNotEmpty()) challenges.random() else null
        } catch (e: Exception) {
            // Si falla la red, usamos Room como backup
            challengeDao.getRandomChallenge()
        }
    }

    // --- Métodos de sincronización dual (Room + Firestore) ---

    override suspend fun insertLocal(challenge: Challenge) {
        challengeDao.insertChallenge(challenge)
    }

    override suspend fun updateLocal(challenge: Challenge) {
        challengeDao.updateChallenge(challenge)
    }

    override suspend fun deleteLocal(challenge: Challenge) {
        challengeDao.deleteChallenge(challenge)
    }

    private fun getChallengesCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("challenges")
    }

    override fun getRemoteChallenges(): Flow<Result<List<Challenge>>> = callbackFlow {
        val collection = getChallengesCollection()
        if (collection == null) {
            trySend(Result.failure(Exception("Usuario no autenticado")))
            close()
            return@callbackFlow
        }

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val challenges = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Challenge::class.java)?.apply { id = doc.id }
            } ?: emptyList()
            trySend(Result.success(challenges))
        }

        awaitClose { listener.remove() }
    }

    override fun addRemoteChallenge(challenge: Challenge): Flow<Result<Challenge>> = callbackFlow {
        val collection = getChallengesCollection()
        if (collection == null) {
            trySend(Result.failure(Exception("Usuario no autenticado")))
            close()
            return@callbackFlow
        }

        val docRef = if (challenge.id.isNotEmpty()) {
            collection.document(challenge.id)
        } else {
            collection.document()
        }

        val finalChallenge = challenge.copy(id = docRef.id)
        docRef.set(finalChallenge)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) trySend(Result.success(finalChallenge))
                else trySend(Result.failure(task.exception ?: Exception("Error al agregar reto remoto")))
                close()
            }
        awaitClose { }
    }

    override fun updateRemoteChallenge(challenge: Challenge): Flow<Result<Unit>> = callbackFlow {
        val collection = getChallengesCollection()
        if (collection == null || challenge.id.isEmpty()) {
            trySend(Result.failure(Exception("ID inválido o usuario no autenticado")))
            close()
            return@callbackFlow
        }

        collection.document(challenge.id).set(challenge)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) trySend(Result.success(Unit))
                else trySend(Result.failure(task.exception ?: Exception("Error al actualizar reto remoto")))
                close()
            }
        awaitClose { }
    }

    override fun deleteRemoteChallenge(challenge: Challenge): Flow<Result<Unit>> = callbackFlow {
        val collection = getChallengesCollection()
        if (collection == null || challenge.id.isEmpty()) {
            trySend(Result.failure(Exception("ID inválido o usuario no autenticado")))
            close()
            return@callbackFlow
        }

        collection.document(challenge.id).delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) trySend(Result.success(Unit))
                else trySend(Result.failure(task.exception ?: Exception("Error al eliminar reto remoto")))
                close()
            }
        awaitClose { }
    }
}
