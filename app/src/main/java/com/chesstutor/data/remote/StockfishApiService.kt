package com.chesstutor.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class StockfishResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "evaluation") val evaluation: Double? = null,
    @Json(name = "mate") val mate: Int? = null,
    @Json(name = "bestmove") val bestMoveRaw: String? = null,
    @Json(name = "continuation") val continuation: String? = null
) {
    val bestMoveUci: String?
        get() {
            val raw = bestMoveRaw ?: return null
            // Formats often like "bestmove e2e4 ponder e7e5" or "e2e4"
            val parts = raw.split(" ")
            return if (parts.size >= 2 && parts[0] == "bestmove") parts[1] else parts[0]
        }
}

interface StockfishApiService {
    @GET("api/s/v2.php")
    suspend fun getEvaluation(
        @Query("fen") fen: String,
        @Query("depth") depth: Int = 12
    ): StockfishResponse
}

