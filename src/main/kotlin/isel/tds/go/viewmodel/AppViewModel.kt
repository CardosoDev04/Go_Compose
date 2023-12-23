package isel.tds.go.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import isel.tds.go.model.Board
import isel.tds.go.model.Clash
import isel.tds.go.model.Game
import isel.tds.go.mongo.MongoDriver
import isel.tds.go.storage.GameSerializer
import isel.tds.go.storage.MongoStorage
import kotlinx.coroutines.CoroutineScope

class AppViewModel(driver: MongoDriver, val scope: CoroutineScope) {
    private val storage = MongoStorage<String, Game>("games", driver, GameSerializer)
    private var clash by mutableStateOf(Clash(storage))

    var viewScore by mutableStateOf(false)
        private set
    var inputName by mutableStateOf<InputName?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    val board: Board? get() = (clash as? ClashRun)?.game?.board
//    val score:
    val me: Player? get() = (clash as? ClashRun)?.me

    val hasClash: Boolean get() = clash is ClashRun
    val newAvailable: Boolean get() = clash.canNewBoard()


}