package com.saintmarina.alphatraining

import org.junit.Assert.*
import org.junit.Test

class IIRFilterTest {
    @Test
    fun filterTest() {
        val b = doubleArrayOf(1.0, -1.0)
        val a = doubleArrayOf(1.0)
        val x = doubleArrayOf(5.0, 5.0, 5.0, 6.0, 5.0, 5.0)
        val y1 = DoubleArray(6)
        val y = DoubleArray(6)
        val f = IIRFilter(b, a)
        val f2 = IIRFilter(b, a)
        for (i in x.indices) {
            y1[i] = f.filter(x[i])
        }
        for (i in y1.indices) {
            y[i] = f2.filter(y1[i])
        }

        val expectedYs = doubleArrayOf(5.0, 0.0, 0.0, 1.0, -1.0, 0.0)
        assertArrayEquals(expectedYs, y, 0.0)
    }
}