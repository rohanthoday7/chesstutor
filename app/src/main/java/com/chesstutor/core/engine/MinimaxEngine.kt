package com.chesstutor.core.engine

import com.chesstutor.core.chess.ChessGameState
import com.chesstutor.core.chess.ChessRules
import com.chesstutor.core.chess.Move
import com.chesstutor.core.chess.PieceColor
import com.chesstutor.core.chess.PieceType
import com.chesstutor.core.chess.PgnUtils
import kotlin.random.Random

data class SearchResult(
    val bestMove: Move?,
    val evaluationCentipawns: Int,
    val nodesEvaluated: Int
)

class MinimaxEngine(private val personality: BotPersonality = BotPersonality.ALL_BOTS[2]) {

    private var nodesCount = 0

    fun calculateBestMove(state: ChessGameState): SearchResult {
        nodesCount = 0
        val legalMoves = ChessRules.getLegalMoves(state)
        if (legalMoves.isEmpty()) {
            return SearchResult(null, 0, 0)
        }

        // 1. Check opening book first
        val movesSans = state.moveHistory.map { it.san }
        val bookSan = OpeningBook.getBookMove(movesSans)
        if (bookSan != null) {
            val bookMove = PgnUtils.parseSanMove(state, bookSan)
            if (bookMove != null && legalMoves.contains(bookMove)) {
                return SearchResult(bookMove, 0, 1)
            }
        }

        // 2. Simulate blunder based on bot personality
        if (personality.blunderProbability > 0f && Random.nextFloat() < personality.blunderProbability) {
            val nonOptimalMoves = legalMoves.filter { !it.isCastling }
            val chosenMove = if (nonOptimalMoves.isNotEmpty()) nonOptimalMoves.random() else legalMoves.random()
            return SearchResult(chosenMove, 0, 1)
        }

        // 3. Alpha-Beta minimax search
        val isWhite = state.turn == PieceColor.WHITE
        var bestMove: Move? = null
        var bestScore = if (isWhite) -999999 else 999999
        var alpha = -999999
        var beta = 999999

        val sortedMoves = orderMoves(state, legalMoves)

        for (move in sortedMoves) {
            val nextState = ChessRules.makeMove(state, move)
            val score = alphaBeta(nextState, personality.searchDepth - 1, alpha, beta, !isWhite)

            if (isWhite) {
                if (score > bestScore) {
                    bestScore = score
                    bestMove = move
                }
                alpha = maxOf(alpha, score)
            } else {
                if (score < bestScore) {
                    bestScore = score
                    bestMove = move
                }
                beta = minOf(beta, score)
            }

            if (beta <= alpha) break
        }

        return SearchResult(bestMove ?: legalMoves[0], bestScore, nodesCount)
    }

    private fun alphaBeta(
        state: ChessGameState,
        depth: Int,
        alphaInit: Int,
        betaInit: Int,
        isMaximizing: Boolean
    ): Int {
        nodesCount++
        var alpha = alphaInit
        var beta = betaInit

        if (state.status.isGameOver) {
            return when (val status = state.status) {
                is com.chesstutor.core.chess.GameStatus.Checkmate -> {
                    if (status.winner == PieceColor.WHITE) 100000 + depth else -100000 - depth
                }
                else -> 0 // Draw
            }
        }

        if (depth <= 0) {
            return quiescenceSearch(state, alpha, beta, isMaximizing, maxDepth = 2)
        }

        val legalMoves = ChessRules.getLegalMoves(state)
        val sortedMoves = orderMoves(state, legalMoves)

        if (isMaximizing) {
            var maxEval = -999999
            for (move in sortedMoves) {
                val nextState = ChessRules.makeMove(state, move)
                val eval = alphaBeta(nextState, depth - 1, alpha, beta, false)
                maxEval = maxOf(maxEval, eval)
                alpha = maxOf(alpha, eval)
                if (beta <= alpha) break
            }
            return maxEval
        } else {
            var minEval = 999999
            for (move in sortedMoves) {
                val nextState = ChessRules.makeMove(state, move)
                val eval = alphaBeta(nextState, depth - 1, alpha, beta, true)
                minEval = minOf(minEval, eval)
                beta = minOf(beta, eval)
                if (beta <= alpha) break
            }
            return minEval
        }
    }

    private fun quiescenceSearch(
        state: ChessGameState,
        alphaInit: Int,
        betaInit: Int,
        isMaximizing: Boolean,
        maxDepth: Int
    ): Int {
        var alpha = alphaInit
        var beta = betaInit

        val standPat = Evaluator.evaluate(state.board)
        if (maxDepth <= 0) return standPat

        if (isMaximizing) {
            if (standPat >= beta) return beta
            alpha = maxOf(alpha, standPat)

            val captures = ChessRules.getLegalMoves(state).filter { state.board[it.to] != null }
            val sortedCaptures = orderMoves(state, captures)

            for (move in sortedCaptures) {
                val nextState = ChessRules.makeMove(state, move)
                val score = quiescenceSearch(nextState, alpha, beta, false, maxDepth - 1)
                alpha = maxOf(alpha, score)
                if (beta <= alpha) break
            }
            return alpha
        } else {
            if (standPat <= alpha) return alpha
            beta = minOf(beta, standPat)

            val captures = ChessRules.getLegalMoves(state).filter { state.board[it.to] != null }
            val sortedCaptures = orderMoves(state, captures)

            for (move in sortedCaptures) {
                val nextState = ChessRules.makeMove(state, move)
                val score = quiescenceSearch(nextState, alpha, beta, true, maxDepth - 1)
                beta = minOf(beta, score)
                if (beta <= alpha) break
            }
            return beta
        }
    }

    private fun orderMoves(state: ChessGameState, moves: List<Move>): List<Move> {
        return moves.sortedByDescending { move ->
            var score = 0
            val target = state.board[move.to]
            val moving = state.board[move.from]

            // MVV-LVA (Most Valuable Victim - Least Valuable Aggressor)
            if (target != null && moving != null) {
                score += target.type.baseValue * 10 - moving.type.baseValue
            }

            // Promotions
            if (move.promotion != null) {
                score += (move.promotion.baseValue ?: 900)
            }

            // Castling bonus
            if (move.isCastling) {
                score += 50
            }

            score
        }
    }
}

