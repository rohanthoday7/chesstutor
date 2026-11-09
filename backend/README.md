# Chessable — Real-Time Multiplayer Backend

A lightweight, real-time WebSocket and REST backend powering live 1v1 matchmaking, move synchronization, clock management, and in-game chat for the Chessable Android application.

## Features
- **Live Matchmaking Queue**: Pairs players automatically based on selected time control (1 min Bullet, 3 min Blitz, 10 min Rapid).
- **WebSocket Protocol**:
  - `JOIN_QUEUE`: Registers player with chosen time control and rating.
  - `MATCH_FOUND`: Emitted when two players are paired; assigns White and Black sides.
  - `MOVE`: Relays UCI moves (`e2e4`, `g1f3`) and synchronized clock timestamps.
  - `RESIGN`: Handles player resignations and declares winner.
  - `DRAW_OFFER`: Relays mutual draw negotiations.
  - `CHAT`: Real-time in-game messaging.
- **REST Health Check**: `GET /health` returns server status, queued player counts, and active rooms.

## Running Locally

### Prerequisites
- Node.js 18+ (tested on Node.js 22/24)
- npm or pnpm

### Quick Start
```bash
# 1. Install dependencies
npm install

# 2. Build TypeScript
npm run build

# 3. Start Server (default port 8080)
npm start
```

### Development Mode (with hot-reload)
```bash
npm run dev
```

### Android Emulator Connectivity
- Android emulators access `localhost` via special IP `10.0.2.2:8080`.
- The Android app is pre-configured with `ws://10.0.2.2:8080/ws` for seamless out-of-the-box local testing.

