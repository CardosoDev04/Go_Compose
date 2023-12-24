import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.LineHeightStyle.Alignment.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import isel.tds.go.mongo.MongoDriver
import isel.tds.go.viewmodel.AppViewModel
import kotlinx.coroutines.coroutineScope


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
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(text)
        }
    }
}

fun main() =
    MongoDriver("Go").use { driver ->
        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "Go",
                state = WindowState(size = DpSize.Unspecified)
            ) {
                App(driver, ::exitApplication)
            }
        }
    }

