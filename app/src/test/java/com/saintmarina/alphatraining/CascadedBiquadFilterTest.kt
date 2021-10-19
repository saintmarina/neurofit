package com.saintmarina.alphatraining

import org.junit.Assert
import org.junit.Test

class CascadedBiquadFilterTest {
    @Test
    fun testFilter() {
        val coefficients: Array<Pair<DoubleArray, DoubleArray>> = arrayOf(Pair(doubleArrayOf(1.0, -1.0), doubleArrayOf(1.0, 0.0)))
        val x = doubleArrayOf(5.0, 5.0, 5.0, 6.0, 5.0, 5.0)
        val y = DoubleArray(6)
        val f = CascadedBiquadFilter(coefficients)

        for (i in x.indices) {
            y[i] = f.filter(x[i])
        }

        val expectedYs = doubleArrayOf(5.0, 0.0, 0.0, 1.0, -1.0, 0.0)
        Assert.assertArrayEquals(expectedYs, y, 0.0)
    }

    @Test
    fun largeValuesTest() {
        var y = 0.0
        val filter = CascadedBiquadFilter(FilterCoefficients.alphaWaves)

        for (i in 0 until 30) {
            y = filter.filter(100000.0)
            println(y)
        }

        Assert.assertTrue("y is too large: $y", kotlin.math.abs(y) < 100000.0)
    }
}