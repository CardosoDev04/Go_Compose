package isel.tds.go.model

import isel.tds.go.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

typealias GameStorage = Storage<String, Game>


open class Clash(val gs: GameStorage)

class ClashRun(
    gs: GameStorage,
    val id: String,
    val me: Piece,
    val game: Game
): Clash(gs)


fun Clash.play(position: Position): Clash {
    check(this is ClashRun) {"Game hasn't started yet!"}
    check(game.turn == me) {"It's not your turn!"}
    val newGame = game.play(position)
    gs.update(id, newGame)

    return ClashRun(gs,id,me,newGame)
}

fun Clash.pass(): Clash {
    check(this is ClashRun) { "Game hasn't started yet!" }
    if (game.turn != me) error("It's not your turn!")
    val newGame = game.pass()
    gs.update(id, newGame)
    return ClashRun(gs,id,me,newGame)
}

fun Clash.start(name: String): Clash{
    val game = Game()
    gs.create(name, game)
    return ClashRun(gs,name,Piece.BLACK,game)
}

fun Clash.join(name: String): Clash{
    val game = gs.read(name) ?: error("Game $name does not exist")
    return ClashRun(gs,name,Piece.WHITE,game)
}

fun Clash.getWinner(): Piece {
    check(this is ClashRun) {"Game hasn't started yet!"}
    check(game.isFinished) {"Game is not finished yet!"}
    return game.getWinner()
}

fun Clash.getScore(): Pair<Int,Double> {
    check(this is ClashRun) {"Game hasn't started yet!"}
    return this.game.score()
}

class NoChangesException: IllegalStateException("No changes were made to the game")
class GameDeletedException: IllegalStateException("Game was deleted")

suspend fun Clash.refresh(): Clash{
    check(this is ClashRun) {"Game hasn't started yet!"}
    val newGame = gs.slowRead(id,1500) ?: throw GameDeletedException()

    if(game.board == newGame.board) throw NoChangesException()
    return ClashRun(gs,id,me,newGame)
}

suspend fun GameStorage.slowRead(key: String, delay: Long): Game? {
    fun log(msg: String) = println(msg)
    log("Starting slow read")
    val result = withContext(Dispatchers.IO){
        Thread.sleep(delay)
        read(key)
    }
    log("Ending slow read")
    return result
}

fun Clash.deleteIfIsOwner(){
    if (this is ClashRun && me == Piece.BLACK)
        gs.delete(id)
}

fun Clash.newBoard(): Clash{
check(this is ClashRun) {"Game hasn't started yet!"}
    val newGame = Game()
    gs.update(id, newGame)
    return ClashRun(gs,id,me,newGame)
}

fun Clash.canNewBoard(): Boolean{
    return this is ClashRun && game.isFinished
}
