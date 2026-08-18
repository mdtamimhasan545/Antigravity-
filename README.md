# 🚀 Local AI Studio - Enterprise Native Android AI Client

A state-of-the-art, feature-packed Native Android AI Chatbot application built using **Kotlin** and **Jetpack Compose (Material 3)**.

---

## ✨ Professional Features

### 🎙️ 1. Voice Input & Audio Text-to-Speech (TTS)
- **Speech-to-Text (STT):** Tap the microphone icon in the input bar and speak your prompt.
- **Text-to-Speech (TTS):** Tap the **"Listen"** button on any assistant message to hear the AI read out loud in natural voice.

### 🎭 2. Multi-Persona Engine
Switch between specialized AI personalities anytime:
- 🧠 **General Genius:** Deep analytical reasoning and structured answers.
- 💻 **Code Architect:** Production-ready code, unit tests, and optimizations.
- 🇧🇩 **Bangla Assistant:** Fluent Bengali conversation, literary compositions, and translations.
- ⚡ **Fast & Concise:** Crisp, direct bullet points with zero fluff.
- ✍️ **Creative Writer:** Imaginative copywriting, storytelling, and scripts.

### 📊 3. Live Inference Speed & Stats
- Real-time token generation metrics (`⚡ 22.4 tok/s • 340 tokens in 1.2s`) displayed under each assistant response.

### 🔍 4. Global Search & Pinning
- Real-time search bar in the drawer to search across all past conversations and messages.
- Pin your most important conversations to the top.

### 📤 5. Export & Share Conversations
- One-tap share button in the top bar to share full conversations via WhatsApp, Gmail, Telegram, or Notes.

### 🔄 6. Regeneration & Response Control
- Instant **Regenerate** button for assistant responses.
- Real-time **Stop Generation** button to halt token streaming immediately.

### 📦 7. Offline GGUF Model Loading
- Select any `.gguf` file from your phone's `Download` folder via Android Storage Access Framework (SAF).
- Fine-tune CPU Hardware Threads, Temperature, Top-P, Max Tokens, and System Prompts.

---

## 📁 Project Structure

```
LocalAIChatApp/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/localaichat/app/
│           ├── MainActivity.kt
│           ├── data/
│           │   ├── audio/ (SpeechManager for TTS/STT)
│           │   ├── model/ (ChatMessage, ChatSession, ModelConfig, Persona)
│           │   └── engine/ (LocalInferenceEngine with metrics)
│           ├── viewmodel/ (ChatViewModel with search, TTS, share, pin)
│           └── ui/
│               ├── theme/ (Color, Theme, Type)
│               ├── components/ (ChatBubble, MessageInputBar, DrawerContent)
│               └── screens/ (ChatScreen, SettingsScreen)
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🛠️ How to Build & Run

### Method 1: Using Android Studio
1. Open **Android Studio**.
2. Click **File > Open** and select:
   `C:\Users\mithu\.gemini\antigravity\scratch\LocalAIChatApp`
3. Connect your Android phone with USB Debugging enabled (or start an Emulator).
4. Click the green **Run (▶)** button.

### Method 2: Build APK via Command Line
Run in the project directory:
```bash
./gradlew assembleDebug
```
The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`
