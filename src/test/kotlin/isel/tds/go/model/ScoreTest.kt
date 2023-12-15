package isel.tds.go.model

import kotlin.test.*

class StoreTest {

    @Test
    fun `Score empty Board`() {
        val sut = Game()
        assertEquals(Pair(0,-3.5), sut.score())
    }

    @Test
    fun `Score with 1 Black Piece`() {
        var sut = Game()
        sut = sut.play("1A".toPosition())
        assertEquals(Pair(0,76.5), sut.score())
    }
    @Test
    fun `Score with 1 White Piece`() {
        var sut = Game()
        sut = sut.pass()
        sut = sut.play("1A".toPosition())
        assertEquals(Pair(80,-3.5), sut.score())
    }

    @Test
    fun `Score with 1 of each Piece`() {
        var sut = Game()
        sut = sut.play("1A".toPosition())
        sut = sut.play("1B".toPosition())
        assertEquals(Pair(0,-3.5), sut.score())
    }

    @Test
    fun `Score in the borders`() {
        var sut = Game()
        sut = sut.play("3A".toPosition())
        sut = sut.play("9H".toPosition())
        sut = sut.play("2B".toPosition())
        sut = sut.play("8I".toPosition())
        sut = sut.play("1C".toPosition())
        assertEquals(Pair(1,-0.5), sut.score())
    }

    @Test
    fun `Score with captures for black`(){
        var sut = Game()
        sut = sut.play("3A".toPosition())
        sut = sut.play("9G".toPosition())
        sut = sut.play("2B".toPosition())
        sut = sut.play("8H".toPosition())
        sut = sut.play("1C".toPosition())
        sut = sut.play("3B".toPosition())
        sut = sut.play("4B".toPosition())
        sut = sut.play("7I".toPosition())
        sut = sut.play("3C".toPosition())
        sut = sut.clean(null)
        assertEquals(Pair(3,1.5), sut.score())

    }

    @Test
    fun `Score with captures for white`(){
        var sut = Game()
        sut = sut.play("8A".toPosition())
        sut = sut.play("1H".toPosition())
        sut = sut.play("1I".toPosition())
        sut = sut.play("2I".toPosition())
        sut = sut.play("9B".toPosition())
        sut = sut.clean(null)
        assertEquals(Pair(2,-2.5), sut.score())

    }

    @Test
    fun `Score with captures for both sides`(){
        var sut = Game()
        sut = sut.play("1A".toPosition())
        sut = sut.play("2A".toPosition())
        sut = sut.play("5E".toPosition())
        sut = sut.play("1B".toPosition())
        sut = sut.clean(null)
        sut = sut.play("3A".toPosition())
        sut = sut.play("4E".toPosition())
        sut = sut.play("2B".toPosition())
        sut = sut.play("5D".toPosition())
        sut = sut.play("1C".toPosition())
        sut = sut.play("6E".toPosition())
        sut = sut.play("1A".toPosition())
        sut = sut.clean(null)
        sut = sut.play("5F".toPosition())
        sut = sut.clean(null)
        assertEquals(Pair(3,0.5), sut.score())

    }
}


