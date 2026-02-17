package com.example.infantguradian

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    // StateFlow holds temperature
    private val _temperature = MutableStateFlow<Double?>(null)
    val temperature: StateFlow<Double?> = _temperature

    // This function will be called from FCM service
    fun updateTemperature(temp: Double?) {
        _temperature.value = temp
    }
}
