package com.saintmarina.alphatraining

class CascadedBiquadFilter(coefficients: Array<Pair<DoubleArray, DoubleArray>>) {
    private val filters = coefficients.map { c -> IIRFilter(c.first, c.second) }

    fun filter(_xn: Double): Double {
        var xn = _xn

        for (f in filters) {
            xn = f.filter(xn)
        }
        return xn
    }
}