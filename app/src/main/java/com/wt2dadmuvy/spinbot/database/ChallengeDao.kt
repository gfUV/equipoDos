package com.wt2dadmuvy.spinbot.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.wt2dadmuvy.spinbot.model.Challenge

/**
 * DAO (Data Access Object) de la entidad Challenge.
 *
 * ¿Qué es un DAO?
 * Es la interfaz que le dice a Room QUÉ operaciones puede hacer sobre la tabla "Challenge"
 * en la base de datos SQLite. Room genera automáticamente el código real de estas funciones
 * al momento de compilar — nosotros solo declaramos qué queremos hacer.
 *
 * ¿Por qué es una interfaz y no una clase?
 * Porque Room la implementa automáticamente. No necesitamos escribir el SQL manualmente.
 */
@Dao
interface ChallengeDao {

    /**
     * Obtiene todos los retos guardados en la base de datos.
     *
     * Retorna un LiveData: esto significa que la lista se actualiza AUTOMÁTICAMENTE
     * en la pantalla cada vez que se agrega, edita o elimina un reto, sin necesidad
     * de hacer consultas manuales.
     *
     * ORDER BY id DESC → el reto más reciente (id más alto) queda primero.
     * Esto cumple el HU 6 Criterio 6: "cada reto nuevo aparece en la parte superior".
     */
    @Query("SELECT * FROM Challenge ORDER BY id DESC")
    fun getAllChallenges(): LiveData<List<Challenge>>

    /**
     * Inserta un nuevo reto en la base de datos.
     * Lo usa HU 7 (Jonatan) cuando el jugador guarda un reto nuevo desde el diálogo.
     *
     * @Insert le dice a Room que genere automáticamente el SQL: INSERT INTO Challenge VALUES(...)
     */
    @Insert
    suspend fun insertChallenge(challenge: Challenge)

    /**
     * Actualiza un reto existente en la base de datos.
     * Lo usa HU 8 (Alexandra) cuando el jugador edita un reto desde el diálogo.
     * Room identifica cuál fila actualizar usando el campo "id" del objeto Challenge.
     *
     * @Update genera: UPDATE Challenge SET name=..., description=... WHERE id=...
     */
    @Update
    suspend fun updateChallenge(challenge: Challenge)

    /**
     * Elimina un reto de la base de datos.
     * Lo usa HU 9 (German) cuando el jugador confirma eliminar un reto desde el diálogo.
     * Room identifica cuál fila eliminar usando el campo "id" del objeto Challenge.
     *
     * @Delete genera: DELETE FROM Challenge WHERE id=...
     */
    @Delete
    suspend fun deleteChallenge(challenge: Challenge)


    /**
     * Obtiene un reto aleatorio desde la base de datos local.
     *
     * HU 12 - Criterio 3:
     * El reto mostrado en el diálogo debe venir desde SQLite/Room y seleccionarse
     * de forma aleatoria.
     */
    @Query("SELECT * FROM Challenge ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomChallenge(): Challenge?

}
