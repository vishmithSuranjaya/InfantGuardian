# Alarm Sound Implementation Guide

## ✅ Current Implementation

The alarm screen now plays background music automatically when displayed!

### Features Implemented:

1. **Automatic Playback**: Sound starts as soon as the alarm screen appears
2. **Looping**: The alarm sound repeats continuously until dismissed
3. **Multiple Stop Triggers**:
   - When X button is pressed
   - When device back button is pressed
   - When activity goes to background (onPause)
   - When activity is destroyed (onDestroy)

4. **System Default Sound**: Currently uses the device's default alarm sound

## 🎵 How It Works

### Code Structure:
```kotlin
class AlarmActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    
    // Starts when screen loads
    override fun onCreate() {
        startAlarmSound()
    }
    
    // Stops when user closes screen
    private fun stopAlarmSound() { ... }
    
    // Cleanup on activity lifecycle events
    override fun onPause() { stopAlarmSound() }
    override fun onDestroy() { stopAlarmSound() }
}
```

### Sound Source:
- Uses `RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)`
- Falls back to notification or ringtone if alarm not available
- Loops continuously until stopped

## 🎼 Adding a Custom Alarm Sound (Optional)

If you want to use a custom alarm sound instead of the system default:

### Step 1: Prepare Your Audio File
- **Format**: MP3 or OGG (recommended)
- **File name**: Use lowercase with underscores (e.g., `alarm_sound.mp3`)
- **Size**: Keep under 1MB for best performance
- **Suggested sounds**: Baby crying, gentle alarm, soothing alert

### Step 2: Add File to Project
1. Place your audio file in: `app/src/main/res/raw/`
2. The file will be accessible as `R.raw.alarm_sound`

### Step 3: Update AlarmActivity.kt
Find this code in `startAlarmSound()` method:
```kotlin
val alarmUri: Uri = try {
    // Uncomment this line when you add alarm_sound.mp3 to res/raw folder
    // Uri.parse("android.resource://$packageName/${R.raw.alarm_sound}")
    
    // For now, use system default alarm sound
    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
```

Change it to:
```kotlin
val alarmUri: Uri = try {
    // Use custom alarm sound from raw folder
    Uri.parse("android.resource://$packageName/${R.raw.alarm_sound}")
```

### Step 4: Rebuild and Test
```powershell
cd "C:\Users\UWU STUDENT\AndroidStudioProjects\InfantGuradian"
.\gradlew assembleDebug
```

## 🔊 Volume Control (Optional Enhancement)

To control the volume, add this to `startAlarmSound()`:

```kotlin
mediaPlayer = MediaPlayer().apply {
    setDataSource(applicationContext, alarmUri)
    setVolume(0.8f, 0.8f) // 80% volume (0.0 to 1.0)
    isLooping = true
    prepare()
    start()
}
```

## 🎚️ Audio Attributes (Optional Enhancement)

For better audio behavior on different Android versions:

```kotlin
import android.media.AudioAttributes

mediaPlayer = MediaPlayer().apply {
    setAudioAttributes(
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    )
    setDataSource(applicationContext, alarmUri)
    isLooping = true
    prepare()
    start()
}
```

## 📝 Testing Checklist

- [ ] Open app and send FCM message
- [ ] Verify alarm screen appears
- [ ] Verify sound starts playing automatically
- [ ] Verify sound loops continuously
- [ ] Press X button - sound should stop
- [ ] Press device back button - sound should stop
- [ ] Let alarm play, lock device - sound should continue
- [ ] Unlock and dismiss - sound should stop

## 🐛 Troubleshooting

### No sound plays:
1. Check device volume (alarm volume, not media volume)
2. Check Do Not Disturb mode is off
3. Check logcat for "AlarmActivity" tag errors
4. Verify audio file exists if using custom sound

### Sound continues after dismissing:
- Should not happen - stopAlarmSound() is called in multiple places
- Check logcat for exceptions

### Sound is too loud/quiet:
- Add volume control as shown above
- Or adjust device alarm volume in system settings

## 🎯 Current Behavior Summary

**When Alarm Screen Opens:**
- ✅ Background music starts automatically
- ✅ Sound loops continuously
- ✅ Uses system default alarm sound

**When User Closes Screen:**
- ✅ Sound stops immediately
- ✅ MediaPlayer resources released
- ✅ Returns to home screen

**Lifecycle Management:**
- ✅ onPause: Stops sound when app goes to background
- ✅ onDestroy: Ensures cleanup when activity destroyed
- ✅ Close button: Explicitly stops sound
- ✅ Back button: Explicitly stops sound

Perfect for alerting parents when baby needs attention! 👶🔔

