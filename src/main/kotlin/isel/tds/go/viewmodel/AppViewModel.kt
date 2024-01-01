package isel.tds.go.viewmodel

import CELL_SIDE
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowPosition.PlatformDefault.y
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
    val lastplay: Position? get() = (clash as? ClashRun)?.game?.lastplay
    val turn: Piece? get() = (clash as? ClashRun)?.game?.turn
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
    private val turnAvailable: Boolean get() = (clash as? ClashRun)?.game?.turn == me || newAvailable // ?

    fun newBoard() { clash = clash.newBoard() }

    fun showScore() { viewScore = true }

    fun showCaptures() { viewCaptures = true }

    fun hideCaptures() { viewCaptures = false }

    fun hideScore() { viewScore = false }

    fun hideError() { errorMessage = null }

    fun play(pos: Position) {
        try {
            clash = clash.play(pos)
            println("Position: [${pos.row},${pos.col}]")
        } catch (e: Exception) {
            errorMessage = e.message
        }
        waitForOtherSide()
    }

    enum class InputName(val txt: String) {
        NEW("Start"), JOIN("Join")
    }

    fun cancelInput() {
        inputName = null
    }

    fun newGame(gameName: String) {
        cancelWaiting()
        clash = clash.start(gameName)
        inputName = null
    }

    fun joinGame(gameName: String) {
        cancelWaiting()
        clash = clash.join(gameName)
        inputName = null
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