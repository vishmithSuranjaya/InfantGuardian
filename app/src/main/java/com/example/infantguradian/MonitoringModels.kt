package com.example.infantguradian

// Minimal data classes to satisfy FCM message mapping and resolve compile errors.
// Fields are nullable or have defaults to be robust when some keys are missing.

data class Sensors(
    val temperature: Double? = null
)

data class BabyCryInfo(
    val prediction: String = "unknown",
    val confidence: Double = 0.0,
    val timestamp: String = ""
)

data class MonitoringData(
    val sensors: Sensors = Sensors(),
    val babyCry: BabyCryInfo = BabyCryInfo(),
    val isAlarmActive: Boolean = false
)

