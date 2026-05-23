package com.wt2dadmuvy.spinbot.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _countdown = MutableLiveData(3)
    val countdown: LiveData<Int> = _countdown

    private var countdownJob: Job? = null

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

    override fun onCleared() {
        countdownJob?.cancel()
        super.onCleared()
    }
}