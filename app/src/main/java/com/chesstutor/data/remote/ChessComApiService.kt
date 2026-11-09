package com.chesstutor.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class ChessComPuzzleResponse(
    @Json(name = "title") val title: String = "",
    @Json(name = "url") val url: String = "",
    @Json(name = "publish_time") val publishTime: Long = 0L,
    @Json(name = "fen") val fen: String = "",
    @Json(name = "pgn") val pgn: String = "",
    @Json(name = "image") val image: String = ""
)

@JsonClass(generateAdapter = true)
data class LeaderboardPlayer(
    @Json(name = "player_id") val playerId: Long = 0L,
    @Json(name = "username") val username: String = "",
    @Json(name = "score") val score: Int = 0,
    @Json(name = "rank") val rank: Int = 0,
    @Json(name = "title") val title: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "country") val country: String? = null
)

@JsonClass(generateAdapter = true)
data class ChessComLeaderboardsResponse(
    @Json(name = "daily") val daily: List<LeaderboardPlayer> = emptyList(),
    @Json(name = "live_rapid") val liveRapid: List<LeaderboardPlayer> = emptyList(),
    @Json(name = "live_blitz") val liveBlitz: List<LeaderboardPlayer> = emptyList(),
    @Json(name = "live_bullet") val liveBullet: List<LeaderboardPlayer> = emptyList(),
    @Json(name = "tactics") val tactics: List<LeaderboardPlayer> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChessComPlayerProfile(
    @Json(name = "username") val username: String = "",
    @Json(name = "name") val name: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "joined") val joined: Long = 0L
)

interface ChessComApiService {
    @GET("pub/puzzle")
    suspend fun getDailyPuzzle(): ChessComPuzzleResponse

    @GET("pub/puzzle/random")
    suspend fun getRandomPuzzle(): ChessComPuzzleResponse

    @GET("pub/leaderboards")
    suspend fun getLeaderboards(): ChessComLeaderboardsResponse

    @GET("pub/player/{username}")
    suspend fun getPlayerProfile(@Path("username") username: String): ChessComPlayerProfile
}

