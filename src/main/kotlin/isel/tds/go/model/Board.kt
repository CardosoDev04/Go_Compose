package isel.tds.go.model

const val BOARD_SIZE = 9
const val BOARD_CELLS = BOARD_SIZE * BOARD_SIZE

/**
 * The position class represents a position in the board.
 * it also stores the symbols used to represent the players.
 */
enum class Piece {
    WHITE, BLACK;
    val other: Piece get() = if (this == WHITE) BLACK else WHITE
    val symbol: String
        get() = when (this) {
            WHITE -> "O"
            BLACK -> "#"
        }
}


/**
 * This class contains the map that constitutes the board.
 */
class Board(
    val boardCells: Map<Position, Piece?> = emptyMap(),
)





