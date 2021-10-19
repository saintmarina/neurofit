package com.saintmarina.alphatraining

import org.junit.Assert.*
import org.junit.Test

class IIRFilterTest {
    @Test
    fun filterTestBs() {
        val b = doubleArrayOf(1.0, -1.0)
        val a = doubleArrayOf(1.0)
        val x = doubleArrayOf(5.0, 5.0, 5.0, 6.0, 5.0, 5.0)
        val y = DoubleArray(6)
        val f = IIRFilter(b, a)
        for (i in x.indices) {
            y[i] = f.filter(x[i])
        }

        val expectedYs = doubleArrayOf(5.0, 0.0, 0.0, 1.0, -1.0, 0.0)
        assertArrayEquals(expectedYs, y, 0.0)
    }

    @Test
    fun filterTestAs() {
        val b = doubleArrayOf(1.0, 2.0)
        val a = doubleArrayOf(1.0, 2.0)
        val x = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        val y = DoubleArray(x.size)
        val f = IIRFilter(b, a)
        for (i in x.indices) {
            y[i] = f.filter(x[i])
        }

        val expectedYs = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        assertArrayEquals(expectedYs, y, 0.0)
    }
}