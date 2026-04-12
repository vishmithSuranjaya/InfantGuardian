package com.example.infantguradian

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
        // StateFlow to hold the CotMobile control result
        private val _cotMobileResult = MutableStateFlow<String?>(null)
        val cotMobileResult: StateFlow<String?> = _cotMobileResult

        // Turn on the CotMobile using the API
        suspend fun turnCotMobileOn(apiUrl: String, authToken: String) {
            val result = CotMobileControlApi.turnCotMobileOn(apiUrl, authToken)
            _cotMobileResult.value = result
        }
    // StateFlow to hold the fan control result
    private val _fanResult = MutableStateFlow<String?>(null)
    val fanResult: StateFlow<String?> = _fanResult

    // Turn on the fan using the API
    suspend fun turnFanOn(apiUrl: String, authToken: String) {
        val result = FanControlApi.turnFanOn(apiUrl, authToken)
        _fanResult.value = result
    }
    // StateFlow holds temperature
    private val _temperature = MutableStateFlow<Double?>(null)
    val temperature: StateFlow<Double?> = _temperature

    // Fetch temperature from Azure IoT Central REST API
    suspend fun fetchTemperatureFromAzure(apiUrl: String, authToken: String) {
        val temp = AzureIoTApi.fetchTemperature(apiUrl, authToken)
        _temperature.value = temp
    }

    // Optionally, keep the old method for manual updates
    fun updateTemperature(temp: Double?) {
        _temperature.value = temp
    }
}
