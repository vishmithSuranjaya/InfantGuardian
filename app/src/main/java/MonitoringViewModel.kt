import androidx.lifecycle.ViewModel
import com.example.infantguradian.FcmDataStore
import com.example.infantguradian.MonitoringData
import com.example.infantguradian.Sensors

class MonitoringViewModel : ViewModel() {

    val data = FcmDataStore.fcmData

    init {
        // Set default state
        FcmDataStore.update(
            MonitoringData(
                sensors = Sensors(temperature = 36.6),
//                babyCry = false,
                isAlarmActive = false
            )
        )
    }
}
