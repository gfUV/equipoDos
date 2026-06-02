package com.wt2dadmuvy.spinbot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.wt2dadmuvy.spinbot.database.AppDatabase
import com.wt2dadmuvy.spinbot.model.Challenge
import com.wt2dadmuvy.spinbot.repository.ChallengeRepository
import kotlinx.coroutines.launch

// ViewModel de la pantalla de retos — AndroidViewModel para acceder al Application context sin fugas de memoria
class ChallengesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChallengeRepository

    // Lista observada por ChallengesFragment; se actualiza automáticamente cuando cambia la BD
    val allChallenges: LiveData<List<Challenge>>

    init {
        val dao = AppDatabase.getDatabase(application).challengeDao()
        repository = ChallengeRepository(dao)
        allChallenges = repository.allChallenges
    }

    fun insert(challenge: Challenge) {
        viewModelScope.launch { repository.insert(challenge) }
    }

    fun update(challenge: Challenge) {
        viewModelScope.launch { repository.update(challenge) }
    }

    fun delete(challenge: Challenge) {
        viewModelScope.launch { repository.delete(challenge) }
    }
}
