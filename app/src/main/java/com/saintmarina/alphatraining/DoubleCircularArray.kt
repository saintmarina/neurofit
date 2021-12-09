package com.saintmarina.alphatraining

class DoubleCircularArray(val size: Int) {
    private var head = 0
    var array: DoubleArray = DoubleArray(size)

    fun push(n: Double) {
        head = (head+1) % size
        array[head] = n
    }

    fun getRelativeToLast(index: Int): Double {
        return array[(head + index + size) % size]
    }
}