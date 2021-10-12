package com.saintmarina.alphatraining

class IIRFilter(private val b: DoubleArray, private val a: DoubleArray) {
    init {
        require(a[0] == 1.0) { "a[0] should be 1" }
    }

    var x = DoubleCircularArray(b.size)
    var y = DoubleCircularArray(a.size)

    fun filter(xn: Double): Double {
        x.push(xn)

        var yn = 0.0
        for (i in 0 until b.size) {
            yn += b[i] * x.getRelativeToLast(-i)
        }
        for (i in 1 until a.size) {
            yn -= a[i] * y.getRelativeToLast(1-i)
        }
        y.push(yn)
        return yn
    }
}
