package com.chesstutor.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

sealed interface MultiplayerEvent {
    data class Matched(val roomId: String, val playerColor: String, val opponentName: String, val opponentRating: Int) : MultiplayerEvent
    data class MoveReceived(val uci: String, val whiteTimeMs: Long, val blackTimeMs: Long) : MultiplayerEvent
    data class OpponentResigned(val winnerColor: String) : MultiplayerEvent
    data class DrawOffered(val fromColor: String) : MultiplayerEvent
    data class ChatMessageReceived(val sender: String, val message: String) : MultiplayerEvent
    data class Error(val message: String) : MultiplayerEvent
    object Disconnected : MultiplayerEvent
}

class MultiplayerClient(private val serverUrl: String = "ws://10.0.2.2:8080/ws") {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private val _events = MutableSharedFlow<MultiplayerEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<MultiplayerEvent> = _events.asSharedFlow()

    fun connect(playerName: String, rating: Int, timeControl: String) {
        val request = Request.Builder().url(serverUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val joinMsg = JSONObject().apply {
                    put("type", "JOIN_QUEUE")
                    put("playerName", playerName)
                    put("rating", rating)
                    put("timeControl", timeControl)
                }
                webSocket.send(joinMsg.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    when (json.optString("type")) {
                        "MATCH_FOUND" -> {
                            _events.tryEmit(
                                MultiplayerEvent.Matched(
                                    roomId = json.getString("roomId"),
                                    playerColor = json.getString("color"),
                                    opponentName = json.getString("opponentName"),
                                    opponentRating = json.getInt("opponentRating")
                                )
                            )
                        }
                        "MOVE" -> {
                            _events.tryEmit(
                                MultiplayerEvent.MoveReceived(
                                    uci = json.getString("uci"),
                                    whiteTimeMs = json.optLong("whiteTimeMs", 0),
                                    blackTimeMs = json.optLong("blackTimeMs", 0)
                                )
                            )
                        }
                        "RESIGN" -> {
                            _events.tryEmit(MultiplayerEvent.OpponentResigned(json.getString("winnerColor")))
                        }
                        "DRAW_OFFER" -> {
                            _events.tryEmit(MultiplayerEvent.DrawOffered(json.getString("fromColor")))
                        }
                        "CHAT" -> {
                            _events.tryEmit(
                                MultiplayerEvent.ChatMessageReceived(
                                    sender = json.getString("sender"),
                                    message = json.getString("message")
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    _events.tryEmit(MultiplayerEvent.Error("Parse error: ${e.message}"))
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _events.tryEmit(MultiplayerEvent.Error("Connection failure: ${t.message}"))
                _events.tryEmit(MultiplayerEvent.Disconnected)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _events.tryEmit(MultiplayerEvent.Disconnected)
            }
        })
    }

    fun sendMove(roomId: String, uci: String, whiteTimeMs: Long, blackTimeMs: Long) {
        val msg = JSONObject().apply {
            put("type", "MOVE")
            put("roomId", roomId)
            put("uci", uci)
            put("whiteTimeMs", whiteTimeMs)
            put("blackTimeMs", blackTimeMs)
        }
        webSocket?.send(msg.toString())
    }

    fun resign(roomId: String, playerColor: String) {
        val msg = JSONObject().apply {
            put("type", "RESIGN")
            put("roomId", roomId)
            put("playerColor", playerColor)
        }
        webSocket?.send(msg.toString())
    }

    fun offerDraw(roomId: String, playerColor: String) {
        val msg = JSONObject().apply {
            put("type", "DRAW_OFFER")
            put("roomId", roomId)
            put("playerColor", playerColor)
        }
        webSocket?.send(msg.toString())
    }

    fun sendChat(roomId: String, sender: String, message: String) {
        val msg = JSONObject().apply {
            put("type", "CHAT")
            put("roomId", roomId)
            put("sender", sender)
            put("message", message)
        }
        webSocket?.send(msg.toString())
    }

    fun disconnect() {
        webSocket?.close(1000, "Leaving game")
        webSocket = null
    }
}

