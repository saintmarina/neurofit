package com.saintmarina.alphatraining

class IIRFilter(private val b: DoubleArray, private val a: DoubleArray) {
    var x = DoubleCircularArray(b.size)
    var y = DoubleCircularArray(a.size)

    fun filter(xn: Double): Double {
        x.push(xn)

        var yn = 0.0
        for (i in 0 until b.size) {
            yn += b[i] * x.getRelativeToLast(-i)
        }

        for (i in 1 until a.size) {
            yn -= a[i] * y.getRelativeToLast(-i+1)
        }

        y.push(yn)
        return yn
    }
}
