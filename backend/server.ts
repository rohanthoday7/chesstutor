import http from "node:http";
import express from "express";
import cors from "cors";
import { WebSocketServer, WebSocket } from "ws";

interface UserAccount {
  id: string;
  username: string;
  email: string;
  passwordHash: string;
  rating: number;
  avatarEmoji: string;
  friends: string[]; // usernames of friends
  friendRequests: string[]; // usernames who sent requests
}

interface QueuedPlayer {
  ws: WebSocket;
  playerName: string;
  rating: number;
  timeControl: string;
}

interface Room {
  id: string;
  white: { ws: WebSocket; name: string; rating: number };
  black: { ws: WebSocket; name: string; rating: number };
  timeControl: string;
}

const app = express();
app.use(cors());
app.use(express.json());

const server = http.createServer(app);
const wss = new WebSocketServer({ server, path: "/ws" });

// In-Memory Database for Users & Friends
const users = new Map<string, UserAccount>();

// Pre-populate with sample users so friends search works immediately!
users.set("alex_gm", {
  id: "u_1",
  username: "Alex_GM",
  email: "alex@chess.com",
  passwordHash: "pass123",
  rating: 2150,
  avatarEmoji: "🦁",
  friends: [],
  friendRequests: []
});
users.set("magnus_fan", {
  id: "u_2",
  username: "Magnus_Fan",
  email: "magnus@chess.com",
  passwordHash: "pass123",
  rating: 1620,
  avatarEmoji: "⚡",
  friends: [],
  friendRequests: []
});
users.set("chess_queen", {
  id: "u_3",
  username: "Chess_Queen",
  email: "queen@chess.com",
  passwordHash: "pass123",
  rating: 1840,
  avatarEmoji: "👑",
  friends: [],
  friendRequests: []
});

// =================== AUTH REST ENDPOINTS ===================

// Register / Create Account
app.post("/api/auth/register", (req, res) => {
  const { username, email, password, startingRating } = req.body;
  if (!username || !email || !password) {
    return res.status(400).json({ error: "Username, email and password are required." });
  }

  const normalizedUser = username.trim().toLowerCase();
  if (users.has(normalizedUser)) {
    return res.status(409).json({ error: "Username already exists. Please choose another." });
  }

  const newUser: UserAccount = {
    id: `u_${Date.now()}`,
    username: username.trim(),
    email: email.trim().toLowerCase(),
    passwordHash: password, // In production, use bcrypt
    rating: startingRating || 1200,
    avatarEmoji: "♟️",
    friends: ["Alex_GM"], // Default friend
    friendRequests: []
  };

  users.set(normalizedUser, newUser);

  res.status(201).json({
    message: "Account created successfully",
    user: {
      id: newUser.id,
      username: newUser.username,
      email: newUser.email,
      rating: newUser.rating,
      avatarEmoji: newUser.avatarEmoji
    },
    token: `token_${newUser.id}`
  });
});

// Login
app.post("/api/auth/login", (req, res) => {
  const { username, password } = req.body;
  if (!username || !password) {
    return res.status(400).json({ error: "Username and password are required." });
  }

  const normalizedUser = username.trim().toLowerCase();
  const user = users.get(normalizedUser);

  if (!user || user.passwordHash !== password) {
    return res.status(401).json({ error: "Invalid username or password." });
  }

  res.json({
    message: "Login successful",
    user: {
      id: user.id,
      username: user.username,
      email: user.email,
      rating: user.rating,
      avatarEmoji: user.avatarEmoji
    },
    token: `token_${user.id}`
  });
});

// =================== FRIENDS REST ENDPOINTS ===================

// Get Friends List
app.get("/api/friends/:username", (req, res) => {
  const normalizedUser = req.params.username.trim().toLowerCase();
  const user = users.get(normalizedUser);

  if (!user) {
    return res.status(404).json({ error: "User not found" });
  }

  const friendsList = user.friends.map((friendName) => {
    const friendAccount = users.get(friendName.toLowerCase());
    return {
      username: friendName,
      rating: friendAccount ? friendAccount.rating : 1350,
      avatarEmoji: friendAccount ? friendAccount.avatarEmoji : "👤",
      isOnline: Math.random() > 0.4
    };
  });

  res.json({
    friends: friendsList,
    requests: user.friendRequests
  });
});

// Add Friend by Username
app.post("/api/friends/add", (req, res) => {
  const { currentUsername, targetUsername } = req.body;
  if (!currentUsername || !targetUsername) {
    return res.status(400).json({ error: "Current username and target username are required." });
  }

  const normCurrent = currentUsername.trim().toLowerCase();
  const normTarget = targetUsername.trim().toLowerCase();

  if (normCurrent === normTarget) {
    return res.status(400).json({ error: "You cannot add yourself as a friend." });
  }

  const currentUser = users.get(normCurrent);
  const targetUser = users.get(normTarget);

  if (!targetUser) {
    return res.status(404).json({ error: `User '${targetUsername}' does not exist.` });
  }

  if (currentUser) {
    if (currentUser.friends.some((f) => f.toLowerCase() === normTarget)) {
      return res.status(400).json({ error: `'${targetUsername}' is already your friend.` });
    }
    currentUser.friends.push(targetUser.username);
    targetUser.friends.push(currentUser.username);
  }

  res.json({
    message: `Added '${targetUser.username}' to friends!`,
    friend: {
      username: targetUser.username,
      rating: targetUser.rating,
      avatarEmoji: targetUser.avatarEmoji,
      isOnline: true
    }
  });
});

// Search Users
app.get("/api/users/search", (req, res) => {
  const query = (req.query.q as string || "").toLowerCase();
  const matches = Array.from(users.values())
    .filter((u) => u.username.toLowerCase().includes(query))
    .slice(0, 5)
    .map((u) => ({
      username: u.username,
      rating: u.rating,
      avatarEmoji: u.avatarEmoji
    }));

  res.json({ users: matches });
});

// Health check
app.get("/health", (req, res) => {
  res.json({
    status: "healthy",
    totalUsers: users.size,
    queuedPlayers: matchmakingQueue.length,
    activeMatches: activeRooms.size,
    timestamp: new Date().toISOString()
  });
});

// =================== WEBSOCKET MULTIPLAYER ===================

const matchmakingQueue: QueuedPlayer[] = [];
const activeRooms = new Map<string, Room>();

wss.on("connection", (ws: WebSocket) => {
  let currentRoomId: string | null = null;

  ws.on("message", (data: string) => {
    try {
      const message = JSON.parse(data.toString());

      switch (message.type) {
        case "JOIN_QUEUE": {
          const player: QueuedPlayer = {
            ws,
            playerName: message.playerName || "Anonymous",
            rating: message.rating || 1200,
            timeControl: message.timeControl || "10 min"
          };

          const opponentIndex = matchmakingQueue.findIndex(
            (p) => p.timeControl === player.timeControl && p.ws !== ws
          );

          if (opponentIndex !== -1) {
            const opponent = matchmakingQueue.splice(opponentIndex, 1)[0];
            const roomId = `room_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
            currentRoomId = roomId;

            const room: Room = {
              id: roomId,
              white: { ws: player.ws, name: player.playerName, rating: player.rating },
              black: { ws: opponent.ws, name: opponent.playerName, rating: opponent.rating },
              timeControl: player.timeControl
            };
            activeRooms.set(roomId, room);

            player.ws.send(
              JSON.stringify({
                type: "MATCH_FOUND",
                roomId,
                color: "WHITE",
                opponentName: opponent.playerName,
                opponentRating: opponent.rating
              })
            );

            opponent.ws.send(
              JSON.stringify({
                type: "MATCH_FOUND",
                roomId,
                color: "BLACK",
                opponentName: player.playerName,
                opponentRating: player.rating
              })
            );
          } else {
            matchmakingQueue.push(player);
          }
          break;
        }

        case "MOVE": {
          const room = activeRooms.get(message.roomId);
          if (room) {
            const recipient = ws === room.white.ws ? room.black.ws : room.white.ws;
            recipient.send(
              JSON.stringify({
                type: "MOVE",
                uci: message.uci,
                whiteTimeMs: message.whiteTimeMs,
                blackTimeMs: message.blackTimeMs
              })
            );
          }
          break;
        }

        case "RESIGN": {
          const room = activeRooms.get(message.roomId);
          if (room) {
            const winner = ws === room.white.ws ? "BLACK" : "WHITE";
            const notification = JSON.stringify({ type: "RESIGN", winnerColor: winner });
            room.white.ws.send(notification);
            room.black.ws.send(notification);
            activeRooms.delete(message.roomId);
          }
          break;
        }

        case "DRAW_OFFER": {
          const room = activeRooms.get(message.roomId);
          if (room) {
            const recipient = ws === room.white.ws ? room.black.ws : room.white.ws;
            recipient.send(JSON.stringify({ type: "DRAW_OFFER", fromColor: message.playerColor }));
          }
          break;
        }

        case "CHAT": {
          const room = activeRooms.get(message.roomId);
          if (room) {
            const recipient = ws === room.white.ws ? room.black.ws : room.white.ws;
            recipient.send(
              JSON.stringify({
                type: "CHAT",
                sender: message.sender,
                message: message.message
              })
            );
          }
          break;
        }
      }
    } catch (err) {
      console.error("WebSocket message handling error:", err);
    }
  });

  ws.on("close", () => {
    const qIndex = matchmakingQueue.findIndex((p) => p.ws === ws);
    if (qIndex !== -1) {
      matchmakingQueue.splice(qIndex, 1);
    }

    if (currentRoomId) {
      const room = activeRooms.get(currentRoomId);
      if (room) {
        const otherWs = ws === room.white.ws ? room.black.ws : room.white.ws;
        otherWs.send(
          JSON.stringify({
            type: "RESIGN",
            winnerColor: ws === room.white.ws ? "BLACK" : "WHITE"
          })
        );
        activeRooms.delete(currentRoomId);
      }
    }
  });
});

const PORT = process.env.PORT || 8080;
server.listen(PORT, () => {
  console.log(`[ChessTutor Backend] Server running on port ${PORT}`);
  console.log(`[ChessTutor Backend] Auth endpoints: POST /api/auth/register, POST /api/auth/login`);
  console.log(`[ChessTutor Backend] Friends endpoints: GET /api/friends/:username, POST /api/friends/add`);
  console.log(`[ChessTutor Backend] WebSocket endpoint: ws://localhost:${PORT}/ws`);
});
