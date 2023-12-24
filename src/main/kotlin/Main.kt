import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import isel.tds.go.model.BOARD_SIZE
import isel.tds.go.model.Board
import isel.tds.go.model.Piece
import isel.tds.go.model.Position
import isel.tds.go.mongo.MongoDriver
import isel.tds.go.viewmodel.AppViewModel

val CELL_SIDE = 100.dp

@Composable
@Preview
fun FrameWindowScope.App(driver: MongoDriver, exitFunction: () -> Unit) {
    var text by remember { mutableStateOf("Hello, World!") }
    val scope = rememberCoroutineScope()
    val vm = remember { AppViewModel(driver, scope) }

    MenuBar {
        Menu("Game") {
            Item("New", onClick = vm::showNewGameDialog)
            Item("Join", onClick = vm::showJoinGameDialog)
            Item("Exit", onClick = exitFunction)
        }
        Menu("Play") {
            Item("Pass", enabled = vm.hasClash, onClick = vm::pass)
            Item("Captures", onClick = vm::showScore) // Ainda n entendi este, provavelmente vai ter a ver com o Compose
            Item("Score", enabled = vm.newAvailable, onClick = vm::newBoard)
        }
//        Menu("Options") {
//            Item("Show Last", onClick = println("Show Last button clicked"))
//        }


    }
    MaterialTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BoardView(vm.board?.boardCells, vm::logClick)
        }
    }
}

//@Composable
//fun BoardView(boardCells: Map<Position, Piece?>?, onClick: (Position)->Unit) {
//    val board = "board.png"
//    Image(
//        painter = painterResource(board),
//        contentDescription = "Board",
//        modifier = Modifier
//            .size(500.dp)
//            .background(
//                color = Color.Black,
//            )
//    )
//}

@Composable
fun BoardView(boardCells: Map<Position, Piece?>?, onClick: (Position) -> Unit) {
    val board = "board.png"

    Box(
        modifier = Modifier
            .size(width = 600.dp, height = 600.dp)
    ) {
        // Background Image
        Image(
            painter = painterResource(board),
            contentDescription = "Board Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        repeat(BOARD_SIZE) { row ->
            Row(
                modifier = Modifier.fillMaxWidth().height(CELL_SIDE),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(BOARD_SIZE) { col ->
                    val pos = Position(row, col.toChar()) // TODO(CHECK COORDINATES)
                    Cell(
                        boardCells?.get(pos),
                        onClick = { onClick(pos) },
                    )
                }
            }
        }
    }
}

@Composable
fun Cell(piece: Piece?, size: Dp = 100.dp, onClick: () -> Unit={}) {
    val modifier = Modifier.size(size)
//        .background(color = Color.White)
    if (piece == null) {
        Box(modifier.clickable(onClick = onClick))
    }
    else {
        val filename = when (piece) {
            Piece.BLACK -> "black.png"
            Piece.WHITE -> "white.png"
        }
        Image(
            painter = painterResource(filename),
            contentDescription = "Piece $piece",
            modifier = modifier
        )
    }
}

// Constants for the board layout
private const val numberOfColumns = 8
private const val numberOfRows = 8



fun main() =
    MongoDriver("Go").use { driver ->
        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "Go",
                state = WindowState(size = DpSize.Unspecified),
            ) {
                App(driver, ::exitApplication)
            }
        }
    }

