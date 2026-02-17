# 🎵 Custom Alarm Sound - Testing Guide

## ✅ Code Updated Successfully

The code has been updated to use your custom `alarm_sound.mp3` file instead of the system default alarm.

---

## 📋 Quick Testing Checklist

### Step 1: Rebuild the App
In Android Studio:
1. **Sync Project**: File → Sync Project with Gradle Files
2. **Clean Build**: Build → Clean Project
3. **Rebuild**: Build → Rebuild Project
4. **Run**: Run → Run 'app' (Shift+F10)

### Step 2: Clear Old App Data (Important!)
Before testing, clear the old app data:
```powershell
adb shell pm clear com.example.infantguradian
```

Or uninstall completely:
```powershell
adb uninstall com.example.infantguradian
```

Then reinstall from Android Studio.

### Step 3: Test the Alarm
1. Open app on your phone
2. Send FCM message to topic `infantguardian`
3. Alarm screen should appear
4. **Listen carefully** - you should hear YOUR custom sound, not the system alarm!

### Step 4: Check the Logs
Monitor the logs while testing:
```powershell
adb logcat -s AlarmActivity
```

**Success output:**
```
AlarmActivity: Custom alarm sound started: alarm_sound.mp3
```

**Failure output (if custom sound doesn't load):**
```
AlarmActivity: Failed to start custom alarm sound, trying system default
AlarmActivity: Fallback to system alarm sound
```

---

## 🔍 How to Confirm It's Working

### Method 1: Listen Carefully
- **System alarm**: Sounds like your phone's clock alarm
- **Custom alarm**: Should be YOUR `alarm_sound.mp3` file

### Method 2: Check Logs in Real-Time
Run this before triggering the alarm:
```powershell
adb logcat -c; adb logcat -s AlarmActivity
```

The log will clearly say either:
- "Custom alarm sound started: alarm_sound.mp3" ✅
- "Fallback to system alarm sound" ❌

### Method 3: Test Different Sounds
To be absolutely sure:
1. Replace `alarm_sound.mp3` with a very different sound
2. Rebuild and reinstall
3. Trigger alarm
4. You should hear the NEW sound

---

## 🐛 If Still Hearing System Default Sound

### Check 1: File Location
Verify file is in the correct location:
```
app/src/main/res/raw/alarm_sound.mp3
```

### Check 2: File Name
- Must be lowercase: `alarm_sound.mp3` ✅
- Not: `Alarm_Sound.mp3` ❌
- Not: `alarm sound.mp3` ❌
- Not: `alarmSound.mp3` ❌

### Check 3: File Format
- MP3 format ✅
- Not corrupted
- Can play in a media player

### Check 4: Clean Build
Sometimes Android Studio caches resources:
```
Build → Clean Project
Build → Rebuild Project
```

### Check 5: Verify APK Contents
After building, check if the file is in the APK:
```powershell
# Find the APK path
adb shell pm path com.example.infantguradian

# The output will be something like:
# package:/data/app/~~xxxx/com.example.infantguradian-xxxx/base.apk

# You can also check in Android Studio:
# Build → Analyze APK...
# Navigate to: res/raw/alarm_sound.mp3
```

### Check 6: Check for Build Errors
Look for any R.raw errors:
```powershell
adb logcat *:E | Select-String "alarm_sound"
```

### Check 7: Try a Different File Name
If `alarm_sound.mp3` doesn't work, try renaming to `baby_alarm.mp3`:

1. Rename the file in `res/raw/` folder
2. Update code in AlarmActivity.kt:
   ```kotlin
   val afd = resources.openRawResourceFd(R.raw.baby_alarm)
   ```
3. Rebuild

---

## 🎯 Testing Commands (All in One)

Run these commands in sequence:

```powershell
# Clear everything
adb logcat -c

# Start monitoring logs
Start-Process powershell -ArgumentList "adb logcat -s AlarmActivity"

# Clear app data
adb shell pm clear com.example.infantguradian

# Launch the app
adb shell am start -n com.example.infantguradian/.MainActivity

# Wait for FCM message, then check logs
```

---

## 📊 Code Comparison

### What Changed:

**OLD CODE (system default):**
```kotlin
val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
mediaPlayer = MediaPlayer().apply {
    setDataSource(applicationContext, alarmUri)
    isLooping = true
    prepare()
    start()
}
```

**NEW CODE (custom sound):**
```kotlin
mediaPlayer = MediaPlayer().apply {
    val afd = resources.openRawResourceFd(R.raw.alarm_sound)
    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
    afd.close()
    
    isLooping = true
    prepare()
    start()
}
```

---

## ✅ Success Indicators

You'll know it's working when:
- ✅ Logs say "Custom alarm sound started: alarm_sound.mp3"
- ✅ Sound is distinctly different from system alarm
- ✅ Sound matches your `alarm_sound.mp3` file
- ✅ No errors in logcat

---

## 📱 Final Test

Do a complete end-to-end test:

1. **Uninstall old version:**
   ```powershell
   adb uninstall com.example.infantguradian
   ```

2. **Rebuild in Android Studio:**
   - Build → Clean Project
   - Build → Rebuild Project

3. **Install fresh:**
   - Run → Run 'app'

4. **Monitor logs:**
   ```powershell
   adb logcat -s AlarmActivity FCM
   ```

5. **Send test FCM message**

6. **Verify:**
   - Alarm screen appears ✅
   - Custom sound plays ✅
   - Logs confirm "Custom alarm sound started" ✅

---

## 🎉 Expected Result

When you trigger the alarm, you should:
1. See the alarm screen with crying baby image
2. Hear **YOUR** custom `alarm_sound.mp3` file
3. Sound loops continuously
4. Pressing X stops the sound
5. Returns to home screen

**No additional libraries needed - everything is built into Android!**

The code is ready. Just rebuild in Android Studio and test! 🚀

