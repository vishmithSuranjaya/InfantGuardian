# 🧪 Quick Test Commands - Close Button Debug

## Run these commands to test the close button

### Step 1: Clear Everything and Start Fresh
```powershell
# Clear logcat
adb logcat -c

# Clear app data
adb shell pm clear com.example.infantguradian

# Launch app
adb shell am start -n com.example.infantguradian/.MainActivity
```

### Step 2: Monitor Logs (Open in separate terminal)
```powershell
adb logcat -s AlarmActivity MainActivity *:E
```

### Step 3: Send Test FCM Message
(Use your FCM testing method to trigger the alarm)

### Step 4: What to Look For

When you click the X button, you should see:
```
AlarmActivity: Close button clicked!
AlarmActivity: Alarm sound stopped
AlarmActivity: Navigating to MainActivity
```

---

## Quick Tests

### Test A: Is Button Clickable?
```powershell
# Monitor only AlarmActivity logs
adb logcat -c ; adb logcat -s AlarmActivity
```

Click the X button. 

✅ **If you see:** `AlarmActivity: Close button clicked!`
→ Button is working, problem is with navigation

❌ **If you DON'T see it:**
→ Button click not detected, UI issue

### Test B: Does Back Button Work?
Press the device back button instead of X.

✅ **If you see:** `AlarmActivity: Back button pressed!`
→ BackHandler works

Compare: If back button works but X doesn't, it's definitely a UI issue with the IconButton.

### Test C: Check for Errors
```powershell
adb logcat *:E | Select-String "AlarmActivity"
```

Look for any errors or exceptions.

---

## One-Line Test Command

Run this single command and then click the X button:
```powershell
adb logcat -c ; adb shell "echo 'Logs cleared. Click X button now...'" ; adb logcat -s AlarmActivity:D AlarmActivity:E
```

---

## Alternative: Add Toast for Visual Feedback

If logs aren't showing anything, let's add a visible Toast message.

### Option 1: Quick Toast Test

I can add this to the code:
```kotlin
import android.widget.Toast

IconButton(
    onClick = {
        Toast.makeText(this@AlarmActivity, "X CLICKED!", Toast.LENGTH_SHORT).show()
        Log.d("AlarmActivity", "Close button clicked!")
        stopAlarmSound()
        navigateToMainActivity()
    },
    // ...
)
```

This will show a popup message when button is clicked, so you'll know for sure if it's working.

---

## Results Interpretation

### Scenario 1: See "Close button clicked!" + Screen closes
✅ **Everything works!** Just rebuild and test again.

### Scenario 2: See "Close button clicked!" + Screen doesn't close
❌ **Navigation issue.** Check:
- MainActivity exists
- No errors in logcat
- Try simpler `finish()` instead of navigation

### Scenario 3: Don't see "Close button clicked!"
❌ **Button not registering clicks.** Possible fixes:
- Button is behind another UI element
- Need to add `.clickable()` modifier
- Need to increase button size
- Try regular Button instead of IconButton

### Scenario 4: Back button works but X doesn't
❌ **IconButton UI issue.** Try:
- Add explicit size: `.size(48.dp)`
- Change to regular Button
- Add background to see if button is visible

---

## Recommended Testing Flow

1. **Rebuild app** (Clean + Rebuild)
2. **Clear app data**
3. **Start log monitoring**:
   ```powershell
   adb logcat -c ; adb logcat -s AlarmActivity
   ```
4. **Open app**
5. **Trigger alarm** (send FCM message)
6. **Click X button**
7. **Watch terminal** - do you see "Close button clicked!"?
8. **Also try back button** - do you see "Back button pressed!"?

---

## 🚨 If Still Not Working

Tell me which of these you see:

- [ ] "Close button clicked!" appears in logs
- [ ] "Back button pressed!" appears when pressing device back
- [ ] Any errors in logcat
- [ ] Screen closes but shows wrong screen
- [ ] Nothing happens at all
- [ ] App crashes

This will help me determine the exact issue!

---

## Quick Fix: Add Visual Feedback

Want me to add a Toast popup so you can see if the button is actually being clicked?

Just say "yes" and I'll add:
```kotlin
Toast.makeText(this@AlarmActivity, "Closing alarm...", Toast.LENGTH_SHORT).show()
```

This will show a popup message when you click X, so we know for certain if the click is registering.

