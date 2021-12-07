package com.saintmarina.alphatraining

import android.content.Context
import android.graphics.*
import android.view.View
import androidx.core.graphics.withMatrix

/*
class WaveVisualizer(context: Context,
                     var values: DoubleCircularArray,
                     private val yMin: Double,
                     private val yMax: Double): View(context) {
    private val paint = Paint().also { it.color = Color.BLACK; it.strokeWidth = 0F; it.style = Paint.Style.STROKE; it.isAntiAlias = true }

    private val path = Path()

    var points = FloatArray((values.size-1)*4)

    // TODO Write a DoubleCircularArrayTransform class that scales values.

    // TODO Remove the `values: DoubleCircularArray` Arugment. Instead, accept a number of points argument (n),

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val n = values.size

        fun yToPixel(y_: Double): Float {
            var y = y_;
            if (y > yMax) y = yMax;
            if (y < yMin) y = yMin;
            return (height/(yMin-yMax) * (y - yMin) + height).toFloat()
        }

        fun xToPixel(x: Int): Float {
            return width/(-n + 1) * (x - n + 1)
        }

        // TODO precompute the X values in the points array, and never touch them again
        var px = xToPixel(0)
        var py = yToPixel(values.getRelativeToLast(0))

        for (i in 1 until (n - 1)) {
            points[4 * (i - 1) + 0] = px
            points[4 * (i - 1) + 1] = py

            px = xToPixel(i)
            py = yToPixel(values.getRelativeToLast(-i))

            points[4 * (i - 1) + 2] = px
            points[4 * (i - 1) + 3] = py
        }

        canvas!!.drawLines(points, paint)
    }
}
 */


class WaveVisualizer(context: Context,
                     var values: DoubleCircularArray,
                     private val yMin: Double,
                     private val yMax: Double): View(context) {
    private val paint = Paint().also {
        it.color = Color.BLACK; it.strokeWidth = 0F; it.style = Paint.Style.STROKE; it.isAntiAlias =
        true
    }

    private val path = Path()

    var points = FloatArray((values.size - 1) * 4)

    // TODO Write a DoubleCircularArrayTransform class that scales values.

    // TODO Remove the `values: DoubleCircularArray` Argument. Instead, accept a number of points argument (n),

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val n = values.size

        // TODO precompute the X values in the points array, and never touch them again
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
        this.invalidate()
    }
}