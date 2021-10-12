package com.saintmarina.alphatraining

import org.junit.Assert.*
import org.junit.Test

class DoubleCircularArrayTest {
    @Test
    fun test() {
        val array = DoubleCircularArray(5)
        array.push(1.0)
        array.push(2.0)
        array.push(3.0)
        assertEquals(2.0, array.getRelativeToLast(-1), 0.0)
        array.push(4.0)
        array.push(5.0)
        array.push(6.0)
        assertEquals(6.0, array.getRelativeToLast(0), 0.0)
        assertEquals(5.0, array.getRelativeToLast(-1),0.0)
        assertEquals(4.0, array.getRelativeToLast(-2), 0.0)
        assertEquals(3.0, array.getRelativeToLast(-3), 0.0)
    }
}