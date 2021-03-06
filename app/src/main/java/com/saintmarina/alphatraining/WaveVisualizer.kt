package com.saintmarina.alphatraining

import android.content.Context
import android.graphics.*
import android.view.View
import androidx.core.graphics.withMatrix

class WaveVisualizer(context: Context, paintColor: Int) : View(context) {
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
        color = paintColor
        strokeWidth = 0F
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val pointsArray:FloatArray = FloatArray(NUM_SECS_ON_SCREEN * 4).apply {
        var counter = 1
        for (i in this.indices) {
            val value:Float = when {
                counter % 4 == 0 -> 1F
                counter % 2 == 0 -> 0F
                else -> (i/4)/NUM_SECS_ON_SCREEN.toFloat()
            }
            this[i] = value
            counter++
        }
    }
    private val gridPaint = Paint().apply {
        color = Color.parseColor("#E0E0E0")
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
            if (py > yMax) py = yMax
            if (py < yMin) py = yMin

            points[4 * (i - 1) + 2] = px
            points[4 * (i - 1) + 3] = py
        }

        // Drawing grid first, so the grids is under the brainwaves
        m.reset()
        m.postScale(width, height)
        canvas!!.withMatrix(m) {
            drawLines(pointsArray, gridPaint)
        }

        m.reset()
        m.preTranslate(0.0f, -yMin)
        m.postScale(width / (-n + 1), height / (yMin - yMax))
        m.postTranslate(width, height)
        canvas.withMatrix(m) {
            drawLines(points, paint)
        }

    }
}