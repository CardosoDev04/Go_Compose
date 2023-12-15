package isel.tds.go.model

/**
 * This class contains information about the game's state.
 * Such as the board, the turn, the score, etc.
 */
class Game (
    val board: Board = Board(),
    val turn: Piece = Piece.BLACK,
    val isFinished: Boolean = false,
    val whiteScore: Int = 0,
    val blackScore: Int = 0,
    val lastWasPast: Boolean = false
    )


/**
 * This function checks if a position is valid when attempting to play.
 */
fun Game.canPlay(pos:Position): Boolean = //
    pos.row in 1..BOARD_SIZE && pos.col in 'A'..<'A' + BOARD_SIZE &&
            this.board.boardCells[pos] == null


/**
 * This function completes a valid play checking if the game has finished and
 * handles all the playing logic.
 */
fun Game.play(pos:Position): Game {
    if (isFinished) {
        println("Game is finished!")
        return this
    }
    require(pos.row in 1..BOARD_SIZE) { "Invalid position" }
    require(pos.col in 'A'..<'A' + BOARD_SIZE) { "Invalid position" }

    if (!this.canPlay(pos)) {
        println("Invalid play, position is occupied!")
        return this
    }

    // We check if the play is suicide
    val isSuicide = this.isSuicide(pos)
    // We check if a piece has liberties after it's player
    val hasLibertiesAfterPlay = this.hasLibertiesAfterPlay(pos)

    // If this play is suicide, meaning it has no liberties after being executed then it's not valid
    if (isSuicide && !hasLibertiesAfterPlay) {
        println("Invalid play, suicide!")
        return this
    }

    // In case the play has liberties after being executed
    else if (hasLibertiesAfterPlay) {
        val newBoardCells = this.board.boardCells.toMutableMap()
        newBoardCells[pos] = this.turn

        // We return the new game, with the modified board given the eliminations
        return Game (
            board = Board(newBoardCells),
            turn = turn.other,
            isFinished = false,
            whiteScore = whiteScore,
            blackScore = blackScore,
            lastWasPast = false
        ).clean(pos)
    }

    val newBoardCells = this.board.boardCells.toMutableMap()
    newBoardCells[pos] = this.turn

    return Game(
        board = Board(newBoardCells),
        turn = turn.other,
        isFinished = isFinished,
        whiteScore = whiteScore,
        blackScore = blackScore,
        lastWasPast = false
    ).clean(null)
}


/**
 * This function is used to determine if a certain position has liberties after it has been played,
 * so that we can determine if a play is suicide, for example.
 */
fun Game.hasLibertiesAfterPlay(pos:Position): Boolean {

    val leftPiece = this.board.boardCells[Position(pos.row, pos.col - 1)]
    val rightPiece = this.board.boardCells[Position(pos.row, pos.col + 1)]
    val upperPiece = this.board.boardCells[Position(pos.row - 1, pos.col)]
    val lowerPiece = this.board.boardCells[Position(pos.row + 1, pos.col)]

    val newBoardCells = this.board.boardCells.toMutableMap()
    newBoardCells[pos] = this.turn

    val newGame = Game(
        board = Board(newBoardCells),
        turn = turn,
        isFinished = isFinished,
        whiteScore = whiteScore,
        blackScore = blackScore,
        lastWasPast = lastWasPast
    ).clean(null)

    return !(newGame.board.boardCells[Position(pos.row, pos.col - 1)] == leftPiece &&
            newGame.board.boardCells[Position(pos.row, pos.col + 1)] == rightPiece &&
            newGame.board.boardCells[Position(pos.row - 1, pos.col)] == upperPiece &&
            newGame.board.boardCells[Position(pos.row + 1, pos.col)] == lowerPiece)

}


/**
 * This function is used to count the liberties of a piece in a certain position.
 */
fun countLiberties(game:Game, pos:Position): Int {
    val visited = mutableSetOf<Position>()

    if (game.board.boardCells[pos] != null)
        return game.exploreLiberties(pos, pos, visited)

    return 0
}

/**
 * This function is used to explore the liberties of a piece in a certain position, it's
 * used as a helper function for the countLiberties function.
 */
private fun Game.exploreLiberties(initialPos: Position, currentPosition: Position, visited: MutableSet<Position>): Int {
    visited.add(currentPosition)

    val adjacentPositions = currentPosition.getAdjacentPositions()
    var liberties = 0

    for (adjacent in adjacentPositions) {
        if (!visited.contains(adjacent) && adjacent.isValidPosition()) {
            if (this.board.boardCells[adjacent] == null)
                liberties++
            else if (this.board.boardCells[adjacent] == this.board.boardCells[initialPos])
                liberties += exploreLiberties(initialPos, adjacent, visited)
        }
    }
    return liberties
}


/**
 * This function is used to check if a given position, if played, results in suicide.
 */
fun Game.isSuicide(pos: Position): Boolean{
    if(pos.isValidPosition() && this.canPlay(pos)) {
        val newBoard = this.board.boardCells.toMutableMap()
        newBoard[pos] = this.turn
        val liberties = Game (
            board = Board(newBoard),
            turn = turn,
            isFinished = isFinished,
            whiteScore = whiteScore,
            blackScore = blackScore,
            lastWasPast = lastWasPast
        ).exploreLiberties(pos,pos, mutableSetOf())
        return liberties == 0
    }
    return false
}

/**
 * This function is used to return a Game object, with a board that has certain positions cleaned.
 */
fun Game.clean(except: Position?): Game {
    val newBoardCells = board.boardCells.toMutableMap()
    var newBlackCaptures = blackScore
    var newWhiteCaptures = whiteScore
    for (r in 1..BOARD_SIZE) {
        for (c in 65..<65 + BOARD_SIZE) {
            if (countLiberties(this, Position(r, c.toChar())) == 0 && board.boardCells[Position(r, c.toChar())] != null && Position(r, c.toChar()) != except) {
                if (board.boardCells[Position(r, c.toChar())] == Piece.WHITE)
                    newBlackCaptures++
                else
                    newWhiteCaptures++

                newBoardCells[Position(r, c.toChar())] = null
            }
        }
    }
    return Game(
        board = Board(newBoardCells),
        turn = turn,
        isFinished = isFinished,
        whiteScore = newWhiteCaptures,
        blackScore = newBlackCaptures,
        lastWasPast = lastWasPast
    )
}


/**
 * This function is used to make the game end.
 */
fun Game.end() {

    val (whiteScore, blackScore) = this.score()
    if (whiteScore > blackScore) {
        println("The winner is player 0 (White). Score = $whiteScore to $blackScore")
    } else if (blackScore > whiteScore) {
        println("The winner is player # (Black). Score = $blackScore to $whiteScore")
    }

}


/**
 * This function handles the resign command logic, for when a player wishes to end the game by quitting.
 */
fun Game.resign(): Game {
    this.end()
    return Game(
        board = Board(board.boardCells),
        turn = turn.other,
        isFinished = true,
        whiteScore = whiteScore,
        blackScore = blackScore,
        lastWasPast = false
    )
}


/**
 * This function is used to calculate the score of the game.
 */
fun Game.score(): Pair<Int, Double> {

    var whiteScore = whiteScore
    var blackScore = blackScore.toDouble()

    //We go through all the pieces in the game
    for (r in 1..BOARD_SIZE) {
        for (c in 65..<65 + BOARD_SIZE) {
            if (board.boardCells[Position(r, c.toChar())] == null) {
                val x = Position(r, c.toChar()).isSurrounded(board)

                if (x == Piece.WHITE) whiteScore++
                else if (x == Piece.BLACK) blackScore++
            }
        }
    }
    //Depending on the board size we will subtract a certain amount of points from the black player
    when(BOARD_SIZE){
        9 -> blackScore -= 3.5
        13 -> blackScore -= 4.5
        19 -> blackScore -= 5.5
    }
    return Pair(whiteScore, blackScore)
}


/**
 * This function handles the passing logic, if two passes are made in a row the game ends.
 */
fun Game.pass(): Game {
    // Check if the game is already finished
    if (isFinished) {
        return this
    }

    if (lastWasPast) {
        this.end()
        return Game (
            board = this.board,
            turn = turn.other,
            isFinished = true,
            whiteScore = whiteScore,
            blackScore = blackScore,
            lastWasPast = true
        )
    } else {
        return Game (
            board = this.board,
            turn = turn.other,
            isFinished = this.isFinished,
            whiteScore = whiteScore,
            blackScore = blackScore,
            lastWasPast = true
        )
    }
}