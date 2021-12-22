package com.saintmarina.alphatraining

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View
import androidx.core.graphics.withMatrix
import java.util.*

class WaveVisualizer(context: Context) : View(context) {
    var values: DoubleCircularArray = DoubleCircularArray(NUM_POINTS_ON_SCREEN)
        set(value) {
            field = value
            points = FloatArray((values.size - 1) * 4)
        }
    private var points = FloatArray((values.size - 1) * 4)
    var yMin = -1.0f
    var yMax = 1.0f

    private val m = Matrix()
    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 0F
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val pointsArray:FloatArray = FloatArray(NUM_SECS_ON_SCREEN * 4)
    private val gridPaint = Paint().apply {
        color = Color.parseColor("#F2F3F4")
        strokeWidth = 2F
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private fun populateWithPoints(pointsArray: FloatArray, height: Float, width: Float){
        var counter = 1

        for (i in pointsArray.indices) {
            val value:Float = when {
                counter % 4 == 0 -> height
                counter % 2 == 0 -> 0F
                else -> (i/4) * (width/NUM_SECS_ON_SCREEN)
            }
            pointsArray[i] = value
            counter++
        }
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
            if (py > yMax) py = yMax
            if (py < yMin) py = yMin

            points[4 * (i - 1) + 2] = px
            points[4 * (i - 1) + 3] = py
        }

        m.reset()
        m.preTranslate(0.0f, -yMin)
        m.postScale(width / (-n + 1), height / (yMin - yMax))
        m.postTranslate(width, height)

        // Drawing grid first, so the grids is under the brainwaves
        populateWithPoints(pointsArray, height, width)
        canvas!!.drawLines(pointsArray, gridPaint)

        canvas.withMatrix(m) {
            drawLines(points, paint)
        }

    }
}