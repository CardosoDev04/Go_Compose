import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import isel.tds.go.mongo.MongoDriver



@Composable
@Preview
fun FrameWindowScope.App(driver: MongoDriver, exitFunction: () -> Unit) {
    var text by remember { mutableStateOf("Hello, World!") }

    MenuBar {
//      Menu("Game") {
////                Item("Start", onClick = println("Start button clicked"))
////                Item("Join", onClick = println("Join game button clicked"))
////            }
////            Menu("Play") {
////                Item("Pass", onClick = println("Pass button clicked"))
////                Item("Captures", onClick = println("Captures button clicked"))
////                Item("Score", onClick = println("Score button clicked"))
////            }
////            Menu("Options") {
////                Item("Show Last", onClick = println("Show Last button clicked"))
////            }
        Menu("Game") {
            Item("Exit", onClick = exitFunction)
        }
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
    }

