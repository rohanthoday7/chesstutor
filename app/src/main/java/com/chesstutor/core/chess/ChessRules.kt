package com.chesstutor.core.chess

import kotlin.math.abs

object ChessRules {

    fun fromFen(fen: String): ChessGameState {
        val parts = fen.trim().split("\\s+".toRegex())
        require(parts.size >= 4) { "Invalid FEN string: insufficient parts ($fen)" }

        val board = Board()
        board.loadFenPiecePlacement(parts[0])

        val turn = if (parts[1] == "w") PieceColor.WHITE else PieceColor.BLACK
        val castling = CastlingRights.fromFenString(parts[2])
        val enPassantSquare = if (parts[3] != "-") Square.fromNotation(parts[3]) else null
        val halfmoveClock = if (parts.size >= 5) parts[4].toIntOrNull() ?: 0 else 0
        val fullmoveNumber = if (parts.size >= 6) parts[5].toIntOrNull() ?: 1 else 1

        val state = ChessGameState(
            board = board,
            turn = turn,
            castlingRights = castling,
            enPassantSquare = enPassantSquare,
            halfmoveClock = halfmoveClock,
            fullmoveNumber = fullmoveNumber
        )

        return state.copy(status = evaluateGameStatus(state))
    }

    fun isSquareAttacked(board: Board, square: Square, attackedBy: PieceColor): Boolean {
        // Pawns
        val pawnDir = attackedBy.pawnDirection
        val pawnAttackRank = square.rank - pawnDir
        if (pawnAttackRank in 0..7) {
            for (df in listOf(-1, 1)) {
                val pawnFile = square.file + df
                if (pawnFile in 0..7) {
                    val piece = board[pawnFile, pawnAttackRank]
                    if (piece != null && piece.color == attackedBy && piece.type == PieceType.PAWN) {
                        return true
                    }
                }
            }
        }

        // Knights
        val knightOffsets = listOf(
            Pair(1, 2), Pair(2, 1), Pair(2, -1), Pair(1, -2),
            Pair(-1, -2), Pair(-2, -1), Pair(-2, 1), Pair(-1, 2)
        )
        for ((df, dr) in knightOffsets) {
            val f = square.file + df
            val r = square.rank + dr
            if (Square.isValid(f, r)) {
                val piece = board[f, r]
                if (piece != null && piece.color == attackedBy && piece.type == PieceType.KNIGHT) {
                    return true
                }
            }
        }

        // Kings
        for (df in -1..1) {
            for (dr in -1..1) {
                if (df == 0 && dr == 0) continue
                val f = square.file + df
                val r = square.rank + dr
                if (Square.isValid(f, r)) {
                    val piece = board[f, r]
                    if (piece != null && piece.color == attackedBy && piece.type == PieceType.KING) {
                        return true
                    }
                }
            }
        }

        // Diagonals (Bishops and Queens)
        val diagonalDirs = listOf(Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1))
        for ((df, dr) in diagonalDirs) {
            var f = square.file + df
            var r = square.rank + dr
            while (Square.isValid(f, r)) {
                val piece = board[f, r]
                if (piece != null) {
                    if (piece.color == attackedBy && (piece.type == PieceType.BISHOP || piece.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                f += df
                r += dr
            }
        }

        // Straights (Rooks and Queens)
        val straightDirs = listOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0))
        for ((df, dr) in straightDirs) {
            var f = square.file + df
            var r = square.rank + dr
            while (Square.isValid(f, r)) {
                val piece = board[f, r]
                if (piece != null) {
                    if (piece.color == attackedBy && (piece.type == PieceType.ROOK || piece.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                f += df
                r += dr
            }
        }

        return false
    }

    fun isInCheck(board: Board, color: PieceColor): Boolean {
        val kingSquare = board.findKing(color) ?: return false
        return isSquareAttacked(board, kingSquare, color.opposite())
    }

    fun getLegalMoves(state: ChessGameState): List<Move> {
        val pseudoMoves = generatePseudoLegalMoves(state)
        return pseudoMoves.filter { isMoveLegal(state, it) }
    }

    fun getLegalMovesForSquare(state: ChessGameState, square: Square): List<Move> {
        val piece = state.board[square] ?: return emptyList()
        if (piece.color != state.turn) return emptyList()
        return getLegalMoves(state).filter { it.from == square }
    }

    private fun isMoveLegal(state: ChessGameState, move: Move): Boolean {
        val testBoard = state.board.copy()
        val movingPiece = testBoard[move.from] ?: return false

        testBoard[move.from] = null
        testBoard[move.to] = if (move.promotion != null) Piece(move.promotion, movingPiece.color) else movingPiece

        // En passant capture removal
        if (move.isEnPassant) {
            val capturedPawnSquare = Square(move.to.file, move.from.rank)
            testBoard[capturedPawnSquare] = null
        }

        // King cannot be in check after own move
        return !isInCheck(testBoard, movingPiece.color)
    }

    fun generatePseudoLegalMoves(state: ChessGameState): List<Move> {
        val moves = mutableListOf<Move>()
        val board = state.board
        val color = state.turn

        for (rank in 0..7) {
            for (file in 0..7) {
                val square = Square(file, rank)
                val piece = board[square] ?: continue
                if (piece.color != color) continue

                when (piece.type) {
                    PieceType.PAWN -> generatePawnMoves(state, square, moves)
                    PieceType.KNIGHT -> generateKnightMoves(board, square, color, moves)
                    PieceType.BISHOP -> generateRayMoves(board, square, color, listOf(Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)), moves)
                    PieceType.ROOK -> generateRayMoves(board, square, color, listOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0)), moves)
                    PieceType.QUEEN -> {
                        generateRayMoves(board, square, color, listOf(Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)), moves)
                        generateRayMoves(board, square, color, listOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0)), moves)
                    }
                    PieceType.KING -> generateKingMoves(state, square, moves)
                }
            }
        }
        return moves
    }

    private fun generatePawnMoves(state: ChessGameState, from: Square, moves: MutableList<Move>) {
        val color = state.turn
        val dir = color.pawnDirection
        val forwardRank = from.rank + dir

        if (forwardRank in 0..7) {
            val forwardSquare = Square(from.file, forwardRank)
            // 1 square forward
            if (state.board[forwardSquare] == null) {
                if (forwardRank == color.promotionRank) {
                    for (promo in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                        moves.add(Move(from, forwardSquare, promotion = promo))
                    }
                } else {
                    moves.add(Move(from, forwardSquare))

                    // 2 squares forward from start rank
                    if (from.rank == color.pawnStartRank) {
                        val doubleSquare = Square(from.file, from.rank + 2 * dir)
                        if (state.board[doubleSquare] == null) {
                            moves.add(Move(from, doubleSquare))
                        }
                    }
                }
            }

            // Diagonal captures
            for (df in listOf(-1, 1)) {
                val targetFile = from.file + df
                if (targetFile in 0..7) {
                    val targetSquare = Square(targetFile, forwardRank)
                    val targetPiece = state.board[targetSquare]
                    if (targetPiece != null && targetPiece.color != color) {
                        if (forwardRank == color.promotionRank) {
                            for (promo in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                                moves.add(Move(from, targetSquare, promotion = promo))
                            }
                        } else {
                            moves.add(Move(from, targetSquare))
                        }
                    } else if (targetSquare == state.enPassantSquare) {
                        // En Passant capture
                        moves.add(Move(from, targetSquare, isEnPassant = true))
                    }
                }
            }
        }
    }

    private fun generateKnightMoves(board: Board, from: Square, color: PieceColor, moves: MutableList<Move>) {
        val offsets = listOf(
            Pair(1, 2), Pair(2, 1), Pair(2, -1), Pair(1, -2),
            Pair(-1, -2), Pair(-2, -1), Pair(-2, 1), Pair(-1, 2)
        )
        for ((df, dr) in offsets) {
            val f = from.file + df
            val r = from.rank + dr
            if (Square.isValid(f, r)) {
                val to = Square(f, r)
                val target = board[to]
                if (target == null || target.color != color) {
                    moves.add(Move(from, to))
                }
            }
        }
    }

    private fun generateRayMoves(
        board: Board,
        from: Square,
        color: PieceColor,
        directions: List<Pair<Int, Int>>,
        moves: MutableList<Move>
    ) {
        for ((df, dr) in directions) {
            var f = from.file + df
            var r = from.rank + dr
            while (Square.isValid(f, r)) {
                val to = Square(f, r)
                val target = board[to]
                if (target == null) {
                    moves.add(Move(from, to))
                } else {
                    if (target.color != color) {
                        moves.add(Move(from, to))
                    }
                    break
                }
                f += df
                r += dr
            }
        }
    }

    private fun generateKingMoves(state: ChessGameState, from: Square, moves: MutableList<Move>) {
        val color = state.turn
        val board = state.board

        for (df in -1..1) {
            for (dr in -1..1) {
                if (df == 0 && dr == 0) continue
                val f = from.file + df
                val r = from.rank + dr
                if (Square.isValid(f, r)) {
                    val to = Square(f, r)
                    val target = board[to]
                    if (target == null || target.color != color) {
                        moves.add(Move(from, to))
                    }
                }
            }
        }

        // Castling checks:
        if (!isInCheck(board, color)) {
            val rank = color.backRank
            if (from == Square(4, rank)) {
                // Kingside
                val canKingside = if (color == PieceColor.WHITE) state.castlingRights.whiteKingside else state.castlingRights.blackKingside
                if (canKingside) {
                    val fSquare = Square(5, rank)
                    val gSquare = Square(6, rank)
                    val hPiece = board[7, rank]
                    if (board[fSquare] == null && board[gSquare] == null &&
                        hPiece?.type == PieceType.ROOK && hPiece.color == color
                    ) {
                        if (!isSquareAttacked(board, fSquare, color.opposite()) &&
                            !isSquareAttacked(board, gSquare, color.opposite())
                        ) {
                            moves.add(Move(from, gSquare, isCastling = true))
                        }
                    }
                }

                // Queenside
                val canQueenside = if (color == PieceColor.WHITE) state.castlingRights.whiteQueenside else state.castlingRights.blackQueenside
                if (canQueenside) {
                    val bSquare = Square(1, rank)
                    val cSquare = Square(2, rank)
                    val dSquare = Square(3, rank)
                    val aPiece = board[0, rank]
                    if (board[bSquare] == null && board[cSquare] == null && board[dSquare] == null &&
                        aPiece?.type == PieceType.ROOK && aPiece.color == color
                    ) {
                        if (!isSquareAttacked(board, cSquare, color.opposite()) &&
                            !isSquareAttacked(board, dSquare, color.opposite())
                        ) {
                            moves.add(Move(from, cSquare, isCastling = true))
                        }
                    }
                }
            }
        }
    }

    fun makeMove(state: ChessGameState, move: Move): ChessGameState {
        val newBoard = state.board.copy()
        val movingPiece = newBoard[move.from] ?: throw IllegalArgumentException("No piece at ${move.from}")
        val capturedPiece: Piece?

        // Execute move on board
        newBoard[move.from] = null

        var nextEnPassant: Square? = null
        var isEnPassantCapture = false

        if (movingPiece.type == PieceType.PAWN) {
            // Check double push
            if (abs(move.to.rank - move.from.rank) == 2) {
                nextEnPassant = Square(move.from.file, (move.from.rank + move.to.rank) / 2)
            } else if (move.to == state.enPassantSquare) {
                // En passant capture
                isEnPassantCapture = true
                val enemyPawnSquare = Square(move.to.file, move.from.rank)
                capturedPiece = newBoard[enemyPawnSquare]
                newBoard[enemyPawnSquare] = null
            }
        }

        capturedPiece = if (isEnPassantCapture) {
            Piece(PieceType.PAWN, state.turn.opposite())
        } else {
            newBoard[move.to]
        }

        // Apply piece or promotion
        newBoard[move.to] = if (move.promotion != null) {
            Piece(move.promotion, movingPiece.color)
        } else {
            movingPiece
        }

        // Handle castling rook movement
        if (move.isCastling || (movingPiece.type == PieceType.KING && abs(move.to.file - move.from.file) == 2)) {
            val rank = state.turn.backRank
            if (move.to.file == 6) { // Kingside
                val rook = newBoard[7, rank]
                newBoard[7, rank] = null
                newBoard[5, rank] = rook
            } else if (move.to.file == 2) { // Queenside
                val rook = newBoard[0, rank]
                newBoard[0, rank] = null
                newBoard[3, rank] = rook
            }
        }

        // Update castling rights
        var whiteK = state.castlingRights.whiteKingside
        var whiteQ = state.castlingRights.whiteQueenside
        var blackK = state.castlingRights.blackKingside
        var blackQ = state.castlingRights.blackQueenside

        if (movingPiece.type == PieceType.KING) {
            if (state.turn == PieceColor.WHITE) {
                whiteK = false
                whiteQ = false
            } else {
                blackK = false
                blackQ = false
            }
        } else if (movingPiece.type == PieceType.ROOK) {
            if (move.from == Square(0, 0)) whiteQ = false
            if (move.from == Square(7, 0)) whiteK = false
            if (move.from == Square(0, 7)) blackQ = false
            if (move.from == Square(7, 7)) blackK = false
        }

        // Also check if a rook was captured
        if (move.to == Square(0, 0)) whiteQ = false
        if (move.to == Square(7, 0)) whiteK = false
        if (move.to == Square(0, 7)) blackQ = false
        if (move.to == Square(7, 7)) blackK = false

        val newCastlingRights = CastlingRights(whiteK, whiteQ, blackK, blackQ)

        // Halfmove clock: reset on pawn move or capture, otherwise increment
        val isPawnMove = movingPiece.type == PieceType.PAWN
        val isCapture = capturedPiece != null
        val nextHalfmove = if (isPawnMove || isCapture) 0 else state.halfmoveClock + 1

        val nextFullmove = if (state.turn == PieceColor.BLACK) state.fullmoveNumber + 1 else state.fullmoveNumber
        val nextTurn = state.turn.opposite()

        // Generate SAN
        val legalMoves = getLegalMoves(state)
        val san = moveToSan(state, move, movingPiece, capturedPiece, newBoard, legalMoves)

        val tempState = ChessGameState(
            board = newBoard,
            turn = nextTurn,
            castlingRights = newCastlingRights,
            enPassantSquare = nextEnPassant,
            halfmoveClock = nextHalfmove,
            fullmoveNumber = nextFullmove
        )

        // Update repetition history
        val repKey = tempState.repetitionKey
        val newOccurrences = state.positionOccurrences.toMutableMap()
        newOccurrences[repKey] = (newOccurrences[repKey] ?: 0) + 1

        val status = evaluateGameStatus(tempState, newOccurrences)

        val record = MoveRecord(
            move = move,
            san = san,
            capturedPiece = capturedPiece,
            fenAfter = tempState.toFen()
        )

        return tempState.copy(
            status = status,
            moveHistory = state.moveHistory + record,
            positionOccurrences = newOccurrences
        )
    }

    private fun evaluateGameStatus(
        state: ChessGameState,
        occurrences: Map<String, Int> = state.positionOccurrences
    ): GameStatus {
        val legalMoves = getLegalMoves(state)
        val inCheck = isInCheck(state.board, state.turn)

        if (legalMoves.isEmpty()) {
            return if (inCheck) {
                // Current player is checkmated; winner is the opposite color
                GameStatus.Checkmate(state.turn.opposite())
            } else {
                GameStatus.Stalemate
            }
        }

        // 50-move rule
        if (state.halfmoveClock >= 100) {
            return GameStatus.DrawFiftyMoves
        }

        // Threefold repetition
        val count = occurrences[state.repetitionKey] ?: 1
        if (count >= 3) {
            return GameStatus.DrawThreefoldRepetition
        }

        // Insufficient material
        if (isInsufficientMaterial(state.board)) {
            return GameStatus.DrawInsufficientMaterial
        }

        return GameStatus.Active
    }

    fun isInsufficientMaterial(board: Board): Boolean {
        val counts = board.countPieces()
        val totalPieces = counts.values.sum()

        // King vs King
        if (totalPieces == 2) return true

        // King + Minor Piece vs King
        if (totalPieces == 3) {
            val hasKnight = counts.keys.any { it.type == PieceType.KNIGHT }
            val hasBishop = counts.keys.any { it.type == PieceType.BISHOP }
            if (hasKnight || hasBishop) return true
        }

        // King + Bishop vs King + Bishop (same colored squares)
        if (totalPieces == 4) {
            val bishops = mutableListOf<Square>()
            for (i in 0 until 64) {
                val p = board[i] ?: continue
                if (p.type == PieceType.BISHOP) {
                    bishops.add(Square.fromIndex(i))
                }
            }
            if (bishops.size == 2) {
                val b1 = bishops[0]
                val b2 = bishops[1]
                if (b1.isLightSquare == b2.isLightSquare) {
                    return true
                }
            }
        }

        return false
    }

    fun moveToSan(
        state: ChessGameState,
        move: Move,
        movingPiece: Piece,
        capturedPiece: Piece?,
        boardAfter: Board,
        legalMoves: List<Move>
    ): String {
        // Castling
        if (move.isCastling || (movingPiece.type == PieceType.KING && abs(move.to.file - move.from.file) == 2)) {
            val base = if (move.to.file == 6) "O-O" else "O-O-O"
            return appendCheckOrMate(base, boardAfter, state.turn.opposite())
        }

        val sb = StringBuilder()

        if (movingPiece.type == PieceType.PAWN) {
            if (capturedPiece != null || move.isEnPassant || move.from.file != move.to.file) {
                sb.append('a' + move.from.file)
                sb.append('x')
            }
            sb.append(move.to.notation)
            if (move.promotion != null) {
                sb.append('=')
                sb.append(move.promotion.symbol.uppercaseChar())
            }
        } else {
            sb.append(movingPiece.type.symbol.uppercaseChar())

            // Disambiguation
            val duplicates = legalMoves.filter {
                it.to == move.to && it.from != move.from && state.board[it.from]?.type == movingPiece.type
            }

            if (duplicates.isNotEmpty()) {
                val sameFile = duplicates.any { it.from.file == move.from.file }
                val sameRank = duplicates.any { it.from.rank == move.from.rank }

                if (!sameFile) {
                    sb.append('a' + move.from.file)
                } else if (!sameRank) {
                    sb.append(move.from.rank + 1)
                } else {
                    sb.append(move.from.notation)
                }
            }

            if (capturedPiece != null) {
                sb.append('x')
            }
            sb.append(move.to.notation)
        }

        return appendCheckOrMate(sb.toString(), boardAfter, state.turn.opposite())
    }

    private fun appendCheckOrMate(baseSan: String, boardAfter: Board, opponentColor: PieceColor): String {
        val opponentInCheck = isInCheck(boardAfter, opponentColor)
        if (!opponentInCheck) return baseSan

        // Check if opponent has any legal move
        val opponentState = ChessGameState(board = boardAfter, turn = opponentColor)
        val opponentLegalMoves = getLegalMoves(opponentState)
        return if (opponentLegalMoves.isEmpty()) {
            "$baseSan#"
        } else {
            "$baseSan+"
        }
    }
}

