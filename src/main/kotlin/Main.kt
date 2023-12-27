import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import isel.tds.go.model.*
import isel.tds.go.mongo.MongoDriver
import isel.tds.go.viewmodel.AppViewModel
import isel.tds.go.viewmodel.AppViewModel.InputName
import sun.java2d.loops.DrawLine

const val BOARD_PNG = "board.png"
const val BLACK_STONE_PNG = "blackStone.png"
const val WHITE_STONE_PNG = "whiteStone.png"
val CELL_SIDE = 60.dp
val BOARD_SIDE = CELL_SIDE * BOARD_SIZE * (BOARD_SIZE - 1)

@Composable
@Preview
fun FrameWindowScope.App(driver: MongoDriver, exitFunction: () -> Unit) {
    val scope = rememberCoroutineScope()
    val vm = remember { AppViewModel(driver, scope) }

    MenuBar {
        Menu("Game") {
            Item("New", onClick = vm::showNewGameDialog)
            Item("Join", onClick = vm::showJoinGameDialog)
            Item("Exit", onClick = { vm.exit(); exitFunction() })
        }
        Menu("Play") {
            Item("Pass", enabled = vm.hasClash, onClick = vm::pass)
            Item("Captures", enabled = vm.hasClash, onClick = vm::showCaptures)
            Item("Score", enabled = vm.isGameOver, onClick = vm::showScore)
        }
//        Menu("Options") {
//            Item("Show Last", onClick = println("Show Last button clicked"))
//        }
    }
    MaterialTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BoardView(vm.board?.boardCells, vm::play)
            StatusBar(vm.clash, vm.me, vm.winner)
        }
        vm.inputName?.let {
            StartOrJoinDialog(it,
                onCancel = vm::cancelInput,
                onAction = if (it == InputName.NEW) vm::newGame else vm::joinGame
            )
        }
        if (vm.viewCaptures){ CapturesDialog(vm.blackCaptures, vm.whiteCaptures, vm::hideCaptures) }
        if (vm.viewScore) { ScoreDialog(vm.score!!, vm::hideScore) }
        vm.errorMessage?.let { ErrorDialog(it, vm::hideError) }
        if (vm.isWaiting) waitingIndicator()
    }
}

@Composable
fun waitingIndicator() = CircularProgressIndicator(
    modifier = Modifier
        .padding(20.dp),
    strokeWidth = 2.dp
)

@Composable
fun StartOrJoinDialog(
    type: InputName,
    onCancel: () -> Unit,
    onAction: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Name to ${type.txt}",
                style = MaterialTheme.typography.h5
            )},
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name of the game") }
            )
        },
        confirmButton = {
            TextButton(
                enabled = true,
                onClick = { onAction(name) }
            ) { Text(type.txt) }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) { Text("Cancel") }
        }
    )
}

@Composable
fun CapturesDialog(blackCaptures: Int, whiteCaptures: Int, closeDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = closeDialog,
        confirmButton = { TextButton(onClick = closeDialog) { Text("Close") } },
        text = {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Piece.entries.forEach { piece ->
                        Row (verticalAlignment = Alignment.CenterVertically) {
                            Cell(piece, size = 20.dp)
                            Text(
                                text = "${piece.name}: ${if (piece == Piece.BLACK) blackCaptures else whiteCaptures}",
                                style = MaterialTheme.typography.h4
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ScoreDialog(score:Pair<Int,Double>, closeDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = closeDialog,
        confirmButton = { TextButton(onClick = closeDialog) { Text("Close") } },
        text = {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Piece.entries.forEach { piece ->
                        Row (verticalAlignment = Alignment.CenterVertically) {
                            Cell(piece, size = 20.dp)
                            Text(
                                text = "${piece.name}: ${if (piece == Piece.BLACK) score.second else score.first}",
                                style = MaterialTheme.typography.h4
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun StatusBar(clash: Clash, me: Piece?, winner: Piece?) {
    Row (
        verticalAlignment = Alignment.CenterVertically
    ){
        me?.let {
            Text("You ", style = MaterialTheme.typography.h4)
            Cell(piece = me, size = 35.dp)
            Spacer(Modifier.width(30.dp))
        }
        val (txt, piece) = when (clash) {
            is ClashRun -> if (!clash.game.isFinished) "Turn: " to clash.game.turn else "Winner: " to winner
            else -> "Game hasn't started yet" to null
        }
        Text(text = txt, style = MaterialTheme.typography.h4)
        Cell(piece = piece, size = 35.dp)
    }
}

@Composable
fun ErrorDialog(message: String, closeDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = closeDialog,
        confirmButton = { TextButton(onClick = closeDialog) { Text("Close") } },
        text = { Text(message) }
    )
}

@Composable
fun BoardView(boardCells: Map<Position, Piece?>?, onClick: (Position) -> Unit) {
    val board = BOARD_PNG

    Box(
        modifier = Modifier
            .size(width = 600.dp, height = 600.dp)
    ) {
        Image(
            painter = painterResource(board),
            contentDescription = "Board Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
        )
        Column(
            modifier = Modifier
                .background(color = Color.Transparent)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Header row with letters A-I
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CELL_SIDE)
            ) {
                // Empty top-left corner square
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f / (BOARD_SIZE + 1))
                )
                repeat(BOARD_SIZE) { col ->
                    Text(
                        text = ('A' + col).toString(),
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f / BOARD_SIZE),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.h5
                    )
                }
            }

            // Rows with numbers 9-1
            repeat(BOARD_SIZE) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CELL_SIDE)
                ) {
                    // Left column with numbers 9-1
                    Text(
                        text = (BOARD_SIZE - row).toString(),
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f / (BOARD_SIZE + 1)), // +1 to account for the letter column
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.h5
                    )

                    // Board cells
                    repeat(BOARD_SIZE) { col ->
                        val pos = Position(BOARD_SIZE - row, ('A' + col))
                        val piece = boardCells?.get(pos)
                        Cell(piece, size = CELL_SIDE, onClick = { onClick(pos) })
                    }
                }
            }
        }
    }
}


@Composable
fun Cell(piece: Piece?, size: Dp = 100.dp, onClick: () -> Unit = {}) {
    val modifier = Modifier
        .size(size)
        .background(color = Color.Transparent)
        .clickable(onClick = onClick)
        .border(1.dp, Color.Black) // Add a border to create grid lines

    Box(
        modifier = modifier
            .border(1.dp, Color.Black) // Add a border to create grid lines
            .padding(2.dp) // Padding is applied to the whole cell
            .fillMaxSize() // Make the Box fill the available space
    ) {
        if (piece != null) {
            val filename = when (piece) {
                Piece.BLACK -> BLACK_STONE_PNG
                Piece.WHITE -> WHITE_STONE_PNG
            }
            Image(
                painter = painterResource(filename),
                contentDescription = "Piece $piece",
                modifier = Modifier.align(Alignment.TopStart) // Align the image to the top-left corner
            )
        }
    }
}

fun main() =
    MongoDriver("Go").use { driver ->
        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "Go",
                state = rememberWindowState(size = DpSize.Unspecified),
            ) {
                App(driver, ::exitApplication)
            }
        }
    }