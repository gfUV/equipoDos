package com.wt2dadmuvy.spinbot.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Challenge(
@PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String
)