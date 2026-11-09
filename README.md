# Chessable — Complete Android Chess Platform (Inspired by Chess.com)

A complete, production-grade Android chess platform inspired by Chess.com. Built with **Kotlin**, **Jetpack Compose (Material 3)**, and **Clean MVVM Architecture**, featuring a FIDE-compliant chess engine, customizable AI bot personalities, real-time multiplayer, official Chess.com Published API integration, and an algorithmic **Chess DNA** player intelligence engine.

---

## 🌟 Key Features

### 1. Complete FIDE Chess Rules Engine
- **Full Legal Move Generation**: Correct move calculation for all pieces (Pawns, Knights, Bishops, Rooks, Queens, Kings).
- **Special Moves**:
  - Kingside & Queenside Castling (with checks for king path obstruction and attacked squares).
  - En Passant captures with one-move expiration.
  - Pawn Promotion modal dialog (Queen, Rook, Bishop, Knight) with auto-promote toggle.
- **Game End Detection**: Checkmate, Stalemate, 50-move rule, Threefold repetition, and Insufficient material (K vs K, K+B vs K, K+N vs K, same-color bishops).
- **FEN & PGN Standard Support**: Load and export FEN position strings and standard PGN game notation.

### 2. Play Against Computer (AI Bots)
- **Minimax with Alpha-Beta Pruning**: Includes quiescence search, piece-square positional tables, pawn structure evaluation, king safety, and mobility bonuses.
- **Distinct Bot Personalities**:
  - 🐣 **Jimmy (600 Elo)**: Beginner bot with high blunder rate, perfect for novices.
  - 🦁 **Nelson (1200 Elo)**: Aggressive Queen attacker playing early Queen incursions.
  - 🎯 **Elena (1600 Elo)**: Tactical opportunist spotting forks, pins, and skewers.
  - ♟️ **Master Alex (2200 Elo)**: Positional International Master squeezing small advantages.
  - 👑 **Magnus Bot (2800 Elo)**: Ruthless grandmaster calculations and endgame mastery.
- **Live Evaluation Bar**: Displays real-time advantage (+2.4, -1.8, M2) alongside the chessboard.
- **Hint Engine**: Stockfish API integration providing suggested moves and visual hints on demand.

### 3. Real-Time Multiplayer
- **Quick Matchmaking**: Match with players for 1 min Bullet, 3 min Blitz, or 10 min Rapid.
- **WebSocket Synchronization**: Live move relay, clock synchronization, draw offers, resignations, and chat.
- **Pass & Play**: Local 2-player board on a single mobile device.

### 4. Puzzles & Puzzle Rush
- **Daily Puzzle**: Fetches official live daily tactics directly from the Chess.com Published API.
- **Offline Tactics Database**: Curated collection of master puzzles for offline training.
- **🔥 Puzzle Rush**: 3-minute timed Blitz challenge mode with streak tracking, 3 strikes maximum, and high score record.

### 5. Opening Explorer & Lessons
- **ECO Code Recognition**: Recognizes 15+ popular chess openings (Ruy Lopez, Sicilian Najdorf, French Defense, Caro-Kann, Queen's Gambit, King's Indian, Italian Game, London System).
- **Step-by-Step Playback**: Interactive move-by-move master lesson navigation with Next/Previous controls.

### 6. 🧬 Chess DNA (Personalized Player Intelligence)
- **6-Dimensional Radar Chart**: Evaluates Tactics, Opening Mastery, Endgame Precision, Aggressiveness, Time Management, and Defensive Resilience.
- **Player Archetype Classification**: Identifies playstyle (e.g., *Dynamic Attacker*, *Classical Strategist*, *Tactical Opportunist*).
- **Automated Diagnosis**: Identifies key strengths, tactical blind spots, and generates targeted study recommendations.

### 7. Global Leaderboards & Social
- **Live Chess.com Leaderboards**: Real-time rankings for Rapid, Blitz, Bullet, and Tactics.
- **Friend Challenges**: Social friends list with online presence indicators.

### 8. Customization & Settings
- **Board Themes**: Classic Green (`#739552`), Wood Finish (`#B37B47`), Dark Charcoal (`#585858`), and Ocean Blue (`#527D9A`).
- **Toggles**: Sound effects, auto-promote Queen, legal move indicators (dots and capture rings), and board coordinate labels.

---

## 🏗️ Project Architecture

```
chesstutor/
├── app/
│   ├── build.gradle.kts           # Compose, Material3, Retrofit, Coroutines
│   ├── src/main/
│   │   ├── AndroidManifest.xml    # Permissions: Internet, Network State, Vibration
│   │   ├── java/com/chesstutor/
│   │   │   ├── ChessApplication.kt
│   │   │   ├── MainActivity.kt    # Root Compose Activity & Navigation
│   │   │   ├── core/
│   │   │   │   ├── chess/         # Board, Piece, Rules, FEN, PGN, SAN
│   │   │   │   ├── engine/        # Minimax, Evaluator, BotPersonalities, OpeningBook
│   │   │   │   └── dna/           # Chess DNA Radar & Archetype Analyzer
│   │   │   ├── data/
│   │   │   │   ├── local/         # Preferences, Themes, Archived Games, Offline Puzzles
│   │   │   │   └── remote/        # Chess.com PubAPI, Stockfish API, WebSocket Client
│   │   │   ├── ui/
│   │   │   │   ├── theme/         # Chess.com Green Palette, Typography, Dark Theme
│   │   │   │   ├── components/    # ChessboardView, EvalBar, Clock, RadarChart, Nav
│   │   │   │   └── screens/       # Home, Play, Puzzles, Learn, Social, Profile, Settings
│   │   │   └── viewmodel/         # GameVM, PuzzleVM, LearnVM, SocialVM, ProfileVM
│   │   └── res/                   # Drawables, Strings, Colors, Themes
│   └── src/test/java/com/chesstutor/
│       ├── ChessRulesTest.kt      # Unit tests: Castling, En Passant, Checkmate, Stalemate
│       └── EngineAndDnaTest.kt    # Unit tests: Minimax, Evaluation, Opening Book, DNA
├── backend/                       # Real-Time WebSocket Multiplayer Node.js Server
│   ├── server.ts
│   ├── package.json
│   └── tsconfig.json
├── gradle/libs.versions.toml      # Dependency Version Catalog
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🚀 How to Open and Run in Android Studio

1. **Launch Android Studio**:
   - Open Android Studio (Ladybug / Koala / Hedgehog or newer).
   - Click **File > Open...** and select `C:\Users\ROHAN\chesst\chesstutor`.

2. **Gradle Sync**:
   - Android Studio will automatically detect `settings.gradle.kts` and sync the project dependencies.
   - Target SDK: **Android 15 (API 35)**; Minimum SDK: **Android 8.0 (API 26)**.
   - Recommended JDK: **JDK 17 or JDK 21** (available in Android Studio Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK).

3. **Run on Emulator or Device**:
   - Select an emulator (e.g. Pixel 8 running Android 14 or 15).
   - Click the green **Run ▶** button (or press `Shift + F10`).

---

## 🌐 Running the Multiplayer Backend (Optional)

To enable live local 1v1 multiplayer matchmaking:

```bash
cd backend
npm install
npm run build
npm start
```

The WebSocket server starts on `ws://localhost:8080/ws`. Android emulators connect automatically via `ws://10.0.2.2:8080/ws`.

---

## 🧪 Running Automated Tests

Run the comprehensive unit test suite in Android Studio:
- Right-click `app/src/test/java/com/chesstutor` and select **Run 'Tests in 'com.chesstutor''**.
- Verifies legal move generation, castling restrictions, en passant removal, checkmate/stalemate identification, minimax calculation, and Chess DNA metric evaluations.

