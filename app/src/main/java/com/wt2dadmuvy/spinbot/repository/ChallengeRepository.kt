package com.wt2dadmuvy.spinbot.repository

import androidx.lifecycle.LiveData
import com.wt2dadmuvy.spinbot.database.ChallengeDao
import com.wt2dadmuvy.spinbot.model.Challenge

/**
 * Repositorio de retos — capa intermedia entre el ViewModel y el DAO.
 *
 * ¿Para qué sirve el Repository en MVVM?
 * El ViewModel no debe hablar directamente con la base de datos.
 * El Repository actúa como intermediario: el ViewModel le pide datos al Repository,
 * y el Repository decide de dónde obtenerlos (base de datos local, internet, etc.).
 * Esto hace el código más organizado, fácil de mantener y testeable.
 *
 * Flujo de datos en esta app:
 *   ChallengesFragment → ChallengesViewModel → ChallengeRepository → ChallengeDao → SQLite
 *
 * @param challengeDao El DAO inyectado desde AppDatabase para acceder a la tabla Challenge.
 */
class ChallengeRepository(private val challengeDao: ChallengeDao) {

    /**
     * Lista de todos los retos como LiveData.
     * Al ser LiveData, la pantalla se actualiza automáticamente cada vez que
     * se agrega, edita o elimina un reto — sin necesidad de recargar manualmente.
     * Los retos vienen ordenados del más reciente al más antiguo (ORDER BY id DESC).
     */
    val allChallenges: LiveData<List<Challenge>> = challengeDao.getAllChallenges()

    /**
     * Inserta un nuevo reto en la base de datos.
     * Al usar "suspend" en el DAO, Room maneja automáticamente la ejecución en un hilo de fondo.
     * Lo llama ChallengesViewModel.insert() desde viewModelScope.
     *
     * @param challenge El objeto Challenge con el nombre y descripción del nuevo reto.
     */
    suspend fun insert(challenge: Challenge) {
        challengeDao.insertChallenge(challenge)
    }

    /**
     * Actualiza un reto existente en la base de datos.
     * Room identifica el reto por su "id" y maneja el hilo de fondo.
     * Lo usará el dialog de editar (HU 8 - Alexandra).
     *
     * @param challenge El objeto Challenge con los datos ya modificados.
     */
    suspend fun update(challenge: Challenge) {
        challengeDao.updateChallenge(challenge)
    }

    /**
     * Elimina un reto de la base de datos.
     * Room identifica el reto por su "id" y maneja el hilo de fondo.
     * Lo usará el dialog de eliminar (HU 9 - German).
     *
     * @param challenge El objeto Challenge que se desea eliminar.
     */
    suspend fun delete(challenge: Challenge) {
        challengeDao.deleteChallenge(challenge)
    }
}
