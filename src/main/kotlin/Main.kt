import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
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
            BoardView(vm.board?.boardCells, vm::logClick)
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
            // Header row with letters A-I and an empty square on the right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CELL_SIDE)
                    .then(Modifier.offset(y = 20.dp, x = -5.dp)),
            ) {
                // Empty top-left corner square
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f / (BOARD_SIZE + 2)) // +2 for the additional empty square on the right
                )
                repeat(BOARD_SIZE) { col ->
                    Text(
                        text = ('A' + col).toString(),
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f / (BOARD_SIZE + 1)),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.h5
                    )
                }
                // Empty square on the right
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f / (BOARD_SIZE + 2)) // +2 for the additional empty square on the right
                )
            }

        }
        Grid(BOARD_SIZE - 1)
        NumberColumn(BOARD_SIZE)

//        // Render the clickable board cells
//        Column(
//            modifier = Modifier
//                .background(color = Color.Transparent)
//                .size(BOARD_SIDE)
//                .then(Modifier.offset(y = CELL_SIDE - 20.dp, x = CELL_SIDE - 20.dp)),
////            verticalArrangement = Arrangement.Center,
//        ) {
//            repeat(BOARD_SIZE) { row ->
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(CELL_SIDE)
//                        .weight(1f / BOARD_SIZE),
////                    horizontalArrangement = Arrangement.spacedBy(0.dp),
//                ) {
//                    repeat(BOARD_SIZE) { col ->
//                        val pos = Position(BOARD_SIZE - row, ('A' + col))
//                        val piece = boardCells?.get(pos)
//                        ClickableCell(piece, size = CELL_SIDE, onClick = { onClick(pos) })
//                    }
//                }
//            }
//        }

        VerticalGrid(
            gridSize = BOARD_SIZE,
            modifier = Modifier
                .background(color = Color.Transparent)
                .size(BOARD_SIDE)
                .then(Modifier.offset(y = CELL_SIDE - 20.dp, x = CELL_SIDE - 20.dp)),
        ) { row, col ->
            val pos = Position(BOARD_SIZE - row, ('A' + col))
            val piece = boardCells?.get(pos)
            ClickableCell(piece, size = CELL_SIDE, onClick = { onClick(pos) })
        }

    }
}

@Composable
fun VerticalGrid(
    gridSize: Int,
    modifier: Modifier = Modifier,
    content: @Composable (row: Int, col: Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        repeat(gridSize) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CELL_SIDE - 4.dp)
            ) {
                repeat(gridSize) { col ->
                    content(row, col)
                }
            }
        }
    }
}

@Composable
fun NumberColumn(size: Int) {
    Column (
        modifier = Modifier
            .offset(y = (CELL_SIDE - 5.dp), x = -(260.dp))

    ){
        for (it in 9 downTo 1){
            Text(
                text = (it).toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CELL_SIDE - 5.dp),


                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h5
            )
        }
    }
}

@Composable
fun Grid(gridSize: Int){
    Row(
        Modifier.offset(y = 70.dp, x = 70.dp)
    ){
        repeat(gridSize) {
            Column {
                repeat(gridSize) {
                    Cell(null, size = CELL_SIDE - 4.dp)
                }
            }
        }
    }
}

@Composable
fun ClickableCell(piece: Piece?, size: Dp = 50.dp, onClick: () -> Unit = {}) {
    val clickableModifier = Modifier
        .size(CELL_SIDE - 4.dp)
        .background(color = Color.Transparent)
        .clickable(onClick = onClick)

    val boxModifier = Modifier
        .size(CELL_SIDE - 4.dp)
//        .padding(2.dp)
        .fillMaxSize()

    Box(
        modifier = clickableModifier
    ) {
        if (piece != null) {
            val filename = when (piece) {
                Piece.BLACK -> "blackStone.png"
                Piece.WHITE -> "whiteStone.png"
            }
            Image(
                painter = painterResource(filename),
                contentDescription = "Piece $piece",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(size)
            )
        }
        // Transparent Box overlay to capture clicks
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        )
    }
}


@Composable
fun Cell(piece: Piece?, size: Dp = 100.dp, onClick: () -> Unit = {}) {
    val modifier = Modifier
        .size(size)
        .background(color = Color.Transparent)
        .border(2.dp, Color.Black) // Add a border to create grid lines

    Box(
        modifier = modifier
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