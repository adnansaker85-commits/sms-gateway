# Pulse Messenger — Advanced Modern Feature Suite & Onboarding Plan

This plan expands Pulse Messenger into a comprehensive next-generation SMS and Gateway platform by adding a first-launch onboarding walkthrough, advanced interactive notifications, and industry-standard advanced SMS capabilities based on modern messenger standards (Google Messages, Microsoft SMS Organizer, and Signal).

---

## 1. Feature Specifications & User Experience

### 1. First-Launch Onboarding Walkthrough (`OnboardingScreen.kt`)
- **First-Launch Guard**: Checked via `SharedPreferences`. Once completed, returning users launch directly into the Inbox.
- **4-Slide Interactive Carousel (`HorizontalPager`)**:
  - **Slide 1: Welcome to Pulse**: Highlights porcelain minimal luxury design, local-first privacy, and zero telemetry tracking.
  - **Slide 2: Smart Auto-Categorization**: Demonstrates real-time sorting into **Personal**, **Transactions & Banking**, **OTP Codes**, and **Updates**.
  - **Slide 3: Instant OTP Notifications & One-Tap Copy**: Showcases automatic code extraction and triggers Android 13+ Notification permission (`POST_NOTIFICATIONS`).
  - **Slide 4: Real-Time Telephony Sync**: Explains background `ContentObserver` syncing and triggers Android Telephony permissions (`READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CONTACTS`).
- **Interactive Controls**: Fluid spring-animated progress indicators, "Skip" button, "Next" tactile button, and "Get Started" finish button.

---

### 2. Advanced Interactive System Notifications (`PulseNotificationHelper.kt`)
- **Notification Channel**: High-importance channel with custom sound and haptic vibration.
- **Dynamic Context-Aware Actions**:
  - **When OTP is detected**:
    - **"📋 Copy Code [XXXXXX]"**: One-tap action button in notification shade. Copies the verification code straight to the Android Clipboard with a toast confirmation.
  - **Inline Direct Reply**:
    - **"💬 Reply"**: Action button using Android `RemoteInput`. Enables typing and sending replies right from the notification bar without opening the app.
  - **"✓ Mark as Read"**: One-tap action button marking the SMS thread as read and dismissing the notification.
  - **Deep Linking**: Tapping the notification body opens `MainActivity` directly into the conversation.

---

### 3. Extra Advanced & Unique Messenger Capabilities

#### A. Scheduled SMS Delivery (Timed Send)
- Long-press the send button or tap the schedule icon in `ChatThreadScreen` to pick a date & time (e.g. birthday wish, delayed reminder).
- Stored locally in Room (`ScheduledMessageEntity`) and scheduled via `WorkManager` or `AlarmManager`.
- Displays pending scheduled messages in the conversation timeline with an edit/cancel affordance.

#### B. Auto-Delete Expired OTPs (24-Hour Expiry Cleaner)
- Automated or one-tap cleanup that purges OTP verification messages older than 24 hours (or 7 days) to eliminate inbox clutter.
- Toggle and "Clean Expired OTPs Now" action button inside Settings.

#### C. Spam & Fraud Shield with Number Blacklist
- On-device heuristic detection for suspicious phishing links, lotteries, and predatory lending keywords.
- Warning badge on high-risk incoming messages.
- Number Blacklist feature in Settings to mute and block unwanted senders.

#### D. Quick Reply Templates & Snippets
- Quick chips above the message composer ("On my way", "Call you in 5 mins", "Received, thanks!") with one-tap transmission.
- Customizable template editor in Settings.

#### E. Conversation Backup & Export (JSON / CSV)
- One-tap export of messages to JSON or CSV via Android system Share Sheet (Save to Drive, Email, Local Files).

---

## 2. Technical Architecture & File Layout

```
app/src/main/
├── AndroidManifest.xml                        // Declares POST_NOTIFICATIONS, receivers, WorkManager
├── java/com/example/
│   ├── notification/
│   │   ├── PulseNotificationHelper.kt        // Notification builder, OTP parsing, RemoteInput
│   │   ├── NotificationActionReceiver.kt     // One-tap copy OTP & Mark as Read receiver
│   │   └── DirectReplyReceiver.kt            // Handles inline replies via SmsManager
│   ├── receiver/
│   │   └── SmsReceiver.kt                    // Incoming SMS listener invoking notification helper
│   ├── service/
│   │   └── ScheduledMessageWorker.kt         // Background worker for scheduled SMS delivery
│   ├── data/
│   │   ├── local/
│   │   │   ├── ScheduledMessageEntity.kt     // Entity for pending scheduled messages
│   │   │   ├── BlockedNumberEntity.kt        // Entity for blacklisted senders
│   │   │   └── PulseDatabase.kt              // Updated Room schema (pins, scheduled, blocked)
│   │   └── repository/
│   │       ├── SmsRepository.kt              // Scheduled SMS & auto-purge OTP integration
│   │       └── BackupHelper.kt               // JSON/CSV export helper
│   ├── ui/
│   │   ├── screens/
│   │   │   ├── OnboardingScreen.kt           // 4-step HorizontalPager with fluid spring styling
│   │   │   ├── ChatThreadScreen.kt           // Updated with schedule dialog & quick reply chips
│   │   │   └── SettingsScreen.kt             // Added Auto-Delete OTPs, Spam Shield, Backup/Export
│   │   └── components/
│   │       └── ScheduleMessageDialog.kt      // Date/time picker dialog for scheduled texts
│   ├── viewmodel/
│   │   └── PulseViewModel.kt                 // State flows for onboarding, scheduled SMS, spam
│   └── MainActivity.kt                       // Onboarding gate router & notification deep-linking
```

---

## 3. Phased Implementation Roadmap

1. **Phase 1: Persistence & Subsystems**
   - Update Room Database (`PulseDatabase.kt`) with `ScheduledMessageEntity` and `BlockedNumberEntity`.
   - Implement `ScheduledMessageWorker.kt` with Android `WorkManager` to trigger `SmsManager` at the scheduled timestamp.
   - Implement `BackupHelper.kt` to export inbox threads to JSON/CSV.

2. **Phase 2: Interactive Notification Engine**
   - Create `PulseNotificationHelper.kt` with High Importance notification channel, OTP regex parser, Copy Action Intent, and `RemoteInput` for Direct Reply.
   - Implement `NotificationActionReceiver.kt` for one-tap clipboard copy and mark-as-read.
   - Implement `DirectReplyReceiver.kt` to parse inline text and send native SMS via `SmsManager`.
   - Update `AndroidManifest.xml` with permissions and receiver declarations.

3. **Phase 3: First-Launch Onboarding Carousel**
   - Create `OnboardingScreen.kt` using Jetpack Compose `HorizontalPager`, step-by-step permission triggers, and porcelain liquid-glass styling.
   - Connect onboarding completion status to `SharedPreferences` in `MainActivity.kt`.

4. **Phase 4: Advanced Messenger UI Enhancements**
   - In `ChatThreadScreen.kt`: Add quick reply templates and the schedule message picker dialog.
   - In `SettingsScreen.kt`: Add controls for "Auto-Delete Expired OTPs after 24h", "Spam & Blocked Numbers", and "Backup & Export".

5. **Phase 5: Verification & Quality Assurance**
   - Run `compile_applet` and test with `gradle :app:testDebugUnitTest`.
   - Verify OTP regex accuracy, scheduled SMS execution logic, and permission flow handling.
