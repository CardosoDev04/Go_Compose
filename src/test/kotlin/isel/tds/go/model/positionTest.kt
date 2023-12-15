package isel.tds.go.model

import kotlin.test.Test
import kotlin.test.assertEquals

class PositionTest {
    @Test
    fun `test normal command`(){
        val sut = Position(0, 'A')
        assertEquals(0, sut.row)
        assertEquals('A', sut.col)
    }

    @Test
    fun `test string to position`() {
        val sut = "3B".toPosition()
        assertEquals(7, sut.row)
        assertEquals('B', sut.col)
    }
}