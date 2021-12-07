package com.saintmarina.alphatraining

import android.content.Context
import android.graphics.*
import android.view.View
import androidx.core.graphics.withMatrix

class WaveVisualizer(context: Context,
                     private val values: DoubleCircularArray,
                     private val yMin: Double,
                     private val yMax: Double) : View(context) {
    var points = FloatArray((values.size - 1) * 4)
    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 0F
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val n = values.size

        var px = 0.0f
        var py = values.getRelativeToLast(0).toFloat()

        for (i in 1 until (n - 1)) {
            points[4 * (i - 1) + 0] = px
            points[4 * (i - 1) + 1] = py

            px = i.toFloat()
            py = values.getRelativeToLast(-i).toFloat()
            if (py > yMax) py = yMax.toFloat()
            if (py < yMin) py = yMin.toFloat()

            points[4 * (i - 1) + 2] = px
            points[4 * (i - 1) + 3] = py
        }

        val m = Matrix()
        m.preTranslate(0.0f, -yMin.toFloat())
        m.postScale(width / (-n + 1), (height / (yMin - yMax)).toFloat())
        m.postTranslate(width, height)

        canvas!!.withMatrix(m) {
            drawLines(points, paint)
        }

        // redraw all the time
        invalidate()
    }
}