package isel.tds.go.storage

import isel.tds.go.model.*

/**
 * This interface is used to serialize and deserialize data, so it can be stored and read according to a pattern.
 */
object GameSerializer: Serializer<Game> {
    /**
     * This function is used to serialize a game, so it is stored according to a pattern.
     */
    override fun serialize(data: Game): String =
        data.board.boardCells.entries.joinToString("|") { (position, piece) ->
            "${position.row}${position.col}:$piece"
        } + "|" + "${data.turn}:${data.isFinished}:${data.whiteScore}:${data.blackScore}:${data.lastWasPast}"

    /**
     * This function is used to deserialize a game, based on the serialized data.
     */
    override fun deserialize(data: String): Game {
        val plays = data.split("|")
        val newCells = mutableMapOf<Position, Piece?>()
        for (play in 0..plays.size-2) {
            val position = plays[play].split(":")[0]
            val piece = plays[play].split(":")[1]
            if (piece != "null") {
                newCells[Position(position[0].toString().toInt(), position[1])] = Piece.valueOf(piece)
            }
        }
        val gameInfo = plays[plays.size - 1].split(':')
        return Game(
            board = Board(newCells),
            turn = Piece.valueOf(gameInfo[0]),
            isFinished = gameInfo[1].toBoolean(),
            whiteScore = gameInfo[2].toInt(),
            blackScore = gameInfo[3].toInt(),
            lastWasPast = gameInfo[4].toBoolean()
        )
    }
}