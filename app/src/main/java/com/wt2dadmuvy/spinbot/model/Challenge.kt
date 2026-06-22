package com.wt2dadmuvy.spinbot.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Modelo unificado para Retos.
 * Se usa tanto para la base de datos local (Room) como para la remota (Firestore).
 *
 * @param id Identificador único. Room lo usa como llave primaria y Firestore como ID del documento.
 * @param name Nombre corto o título del reto.
 * @param description Texto completo del reto.
 */
@Entity
data class Challenge(
    @PrimaryKey
    var id: String = "",
    val name: String = "",
    val description: String = ""
)
