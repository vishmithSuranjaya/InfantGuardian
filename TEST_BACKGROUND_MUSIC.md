# 🧪 Test Script for Background Music Feature

## Prerequisites
- Device connected via USB with ADB
- App installed on device
- FCM configured with topic `infantguardian`

## Test 1: Verify Music Plays on Alarm

### Steps:
1. **Open the app** on your device
2. **Keep app in foreground** (on the home screen with baby image)
3. **Send FCM message** using this curl command:

```powershell
# Replace YOUR_SERVER_KEY with your Firebase Server Key
$serverKey = "YOUR_SERVER_KEY"
$topic = "infantguardian"

$headers = @{
    "Authorization" = "key=$serverKey"
    "Content-Type" = "application/json"
}

$body = @{
    "to" = "/topics/$topic"
    "notification" = @{
        "title" = "Baby Alert!"
        "body" = "Baby needs attention"
    }
    "data" = @{
        "temperature" = "37.5"
        "prediction" = "discomfort"
        "confidence" = "0.89"
        "isAlarmActive" = "true"
        "timestamp" = (Get-Date -Format "yyyy-MM-ddTHH:mm:ss")
    }
} | ConvertTo-Json

Invoke-RestMethod -Uri "https://fcm.googleapis.com/fcm/send" -Method Post -Headers $headers -Body $body
```

4. **Observe**:
   - ✅ Alarm screen should appear immediately
   - ✅ Music should start playing (system alarm sound)
   - ✅ Music should loop continuously
   - ✅ Screen shows crying baby image, red background, alert data

5. **Press X button** (top-right corner)
   - ✅ Music should stop immediately
   - ✅ Should return to home screen

## Test 2: Verify Music Stops with Back Button

### Steps:
1. Send FCM message (alarm screen appears + music plays)
2. **Press device back button**
   - ✅ Music should stop
   - ✅ Should return to home screen

## Test 3: Verify Music Stops When App Goes to Background

### Steps:
1. Send FCM message (alarm screen appears + music plays)
2. **Press home button** to send app to background
   - ✅ Music should stop
3. Reopen app
   - ✅ Should show MainActivity (not alarm screen)

## Test 4: Monitor Logs

### Monitor in real-time:
```powershell
adb logcat -s AlarmActivity FCM
```

### Expected log output:
```
AlarmActivity: Alarm sound started
AlarmActivity: Alarm sound stopped
```

### When FCM message received:
```
FCM: Received message title=Baby Alert! body=Baby needs attention
AlarmActivity: Alarm sound started
```

### When alarm dismissed:
```
AlarmActivity: Alarm sound stopped
```

## Test 5: Volume Check

### If you can't hear the sound:

1. **Check device alarm volume** (not media volume):
   ```powershell
   # Check current alarm volume
   adb shell settings get system alarm_volume
   
   # Set alarm volume to maximum (value depends on device, usually 0-7)
   adb shell settings put system alarm_volume 7
   ```

2. **Check Do Not Disturb mode**:
   - Open device Settings → Sound → Do Not Disturb
   - Make sure it's OFF or allows alarms

3. **Test with logcat**:
   - Even if silent, logs should show "Alarm sound started"

## Common Issues & Solutions

### Issue: No sound plays
**Check:**
- Device not muted
- Alarm volume turned up (not just media volume)
- Do Not Disturb mode is OFF
- Check logcat for errors

**Solution:**
```powershell
# Increase alarm volume
adb shell settings put system alarm_volume 7

# Check for errors
adb logcat -s AlarmActivity:E
```

### Issue: Sound continues after closing
**Should not happen** - sound stops in multiple places:
- Close button onClick
- BackHandler
- onPause
- onDestroy

**Check logs:**
```powershell
adb logcat -s AlarmActivity | Select-String "stopped"
```

### Issue: App crashes when alarm opens
**Check logs:**
```powershell
adb logcat *:E | Select-String "AlarmActivity"
```

**Common cause:** MediaPlayer initialization failed
**Solution:** Code has try-catch blocks, should fallback gracefully

## Verification Checklist

Run through this checklist:

- [ ] App opens successfully
- [ ] Send FCM message from server/Firebase Console
- [ ] Alarm screen appears with red background
- [ ] Crying baby image visible
- [ ] Alert data displayed in cards
- [ ] **Music starts playing immediately** 🎵
- [ ] **Music loops continuously**
- [ ] Press X button → music stops
- [ ] Press device back → music stops
- [ ] Alarm screen closes properly
- [ ] Returns to home screen (MainActivity)
- [ ] No crashes or errors in logcat

## Quick Test Commands

### 1. Clear app data and restart:
```powershell
adb shell pm clear com.example.infantguradian
adb shell am start -n com.example.infantguradian/.MainActivity
```

### 2. Watch for FCM token:
```powershell
adb logcat -s FCM | Select-String "token"
```

### 3. Monitor alarm activity:
```powershell
adb logcat -s AlarmActivity
```

### 4. Full debug logs:
```powershell
adb logcat -s AlarmActivity:D FCM:D *:E
```

## Success Criteria

✅ **All tests pass** = Background music feature working correctly!

Your alarm screen should:
1. Open automatically when FCM message received
2. Play alarm sound immediately
3. Loop the sound continuously
4. Display alert information clearly
5. Stop sound when dismissed (X or Back)
6. Return to home screen properly
7. Clean up resources (no memory leaks)

---

## 🎉 When All Tests Pass

Your InfantGuardian app now has a fully functional alarm system with:
- Visual alert (red screen, crying baby)
- Audio alert (looping alarm sound)
- FCM integration (remote notifications)
- Proper lifecycle management
- User-friendly dismiss mechanism

Ready for real-world use to keep babies safe! 👶🔔

