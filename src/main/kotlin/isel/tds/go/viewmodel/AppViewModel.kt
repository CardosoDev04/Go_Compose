package isel.tds.go.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import isel.tds.go.model.*
import isel.tds.go.mongo.MongoDriver
import isel.tds.go.storage.GameSerializer
import isel.tds.go.storage.MongoStorage
import kotlinx.coroutines.*

class AppViewModel(driver: MongoDriver, val scope: CoroutineScope) {
    private val storage = MongoStorage<String, Game>("games", driver, GameSerializer)
    var clash by mutableStateOf(Clash(storage))  // TODO(Vamos ter q mudar pq o clash tem que ser privado)
        private set
    var viewScore by mutableStateOf(false)
        private set
    var viewCaptures by mutableStateOf(false)
        private set
    var inputName by mutableStateOf<InputName?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    val board: Board? get() = (clash as? ClashRun)?.game?.board
    val whiteCaptures: Int get() = (clash as? ClashRun)?.game?.whiteScore ?: 0
    val blackCaptures: Int get() = (clash as? ClashRun)?.game?.blackScore ?: 0
    val me: Piece? get() = (clash as? ClashRun)?.me
    val score: Pair<Int, Double>? get() = (clash as? ClashRun)?.game?.score()
    val hasClash: Boolean get() = clash is ClashRun
    val winner: Piece? get() = (clash as? ClashRun)?.game?.getWinner()
    val newAvailable: Boolean get() = clash.canNewBoard()
    val isGameOver: Boolean get() = (clash as? ClashRun)?.game?.isFinished ?: false
    private var waitingJob by mutableStateOf<Job?>(null)
    val isWaiting: Boolean get() = waitingJob != null
    var viewMove: Boolean = false
    private val turnAvailable: Boolean get() = (clash as? ClashRun)?.game?.turn == me || newAvailable // ?
    var showLast by mutableStateOf(false)
        private set
    val lastPlay: Position? get() = clash.getLastPlay()
    fun newBoard() { clash = clash.newBoard() }

    fun showScore() { viewScore = true }

    fun showCaptures() { viewCaptures = true }

    fun showLast() { showLast = true }

    fun hideCaptures() { viewCaptures = false }

    fun hideScore() { viewScore = false }

    fun hideError() { errorMessage = null }

    fun hideLast() { showLast = false }

    fun play(pos: Position) {
        try {
            clash = clash.play(pos)
        } catch (e: Exception) {
            errorMessage = e.message
        }
        waitForOtherSide()
    }

    enum class InputName(val txt: String) { NEW("Start"), JOIN("Join") }

    fun cancelInput() {
        inputName = null
    }

    fun newGame(gameName: String) {
        try {
            cancelWaiting()
            clash = clash.start(gameName)
            inputName = null
        }
        catch (e: Exception) {
            errorMessage = e.message
        }
    }

    fun joinGame(gameName: String) {
        try {
            cancelWaiting()
            clash = clash.join(gameName)
            inputName = null
        }
        catch (e: Exception) {
            errorMessage = e.message
        }
        waitForOtherSide()
    }

    fun pass() {
        try {
            clash = clash.pass()
        } catch (e: Exception) {
            errorMessage = e.message
        }
        waitForOtherSide()
    }

    suspend fun refreshGame() {
        try {
            clash = clash.refresh()
        } catch (e: Exception) {
            errorMessage = e.message
        }
    }

    fun showNewGameDialog() {
        inputName = InputName.NEW
    }

    fun showJoinGameDialog() {
        inputName = InputName.JOIN
    }

    fun exit() {
        clash.deleteIfIsOwner()
        cancelWaiting()
    }

    private fun cancelWaiting() {
        waitingJob?.cancel()
        waitingJob = null
    }

    private fun waitForOtherSide() {
        if (turnAvailable) return
        waitingJob = scope.launch(Dispatchers.IO) {
            do {
                delay(3000)
                try {
                    clash = clash.refresh()
                } catch (e: Exception) {
                    errorMessage = e.message
                    if (e is GameDeletedException) clash = Clash(storage)
                }
            } while (!turnAvailable)
            waitingJob = null
        }
    }

    fun logClick(pos: Position) {
        println("Position: [${pos.row},${pos.col}]")
    }
}