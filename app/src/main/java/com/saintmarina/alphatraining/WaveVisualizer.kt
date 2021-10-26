package com.saintmarina.alphatraining

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import android.view.View
import android.view.ViewGroup
import kotlin.math.roundToInt
// Fill the whole canvas with the lines(scale the values by height)
// yLim (what is I want yLim min and yLim max)

// b = offset = max(yMax, abs(yMin))
// m = height/(2*offset)

// scaledY = my(height/2*offset) +b(offset)
// scaledY = y * height/(2*offset) + height*offset
// // m = height/(2*offset)

class WaveVisualizer(context: Context, var values: DoubleCircularArray,var valuesSize:Int, yMax: Double, yMin: Double): View(context) {
    private val paint = Paint().also { it.color = Color.BLACK; it.strokeWidth = 5F; it.style = Paint.Style.STROKE }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val canvas: Canvas = canvas!!
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()



        var j = 0
        val path = Path()
        path.moveTo(width, (scaledY(values.getRelativeToLast(0)).toFloat()+10)*scale)
        for (i in 1 until valuesSize) {
            path.lineTo(width - j, (scaledY(values.getRelativeToLast(-i)))
            j += 20
        }
        canvas.drawPath(path, paint)
    }

    fun scaledY(y: Double): Float {
        val scale = 10F
        return .toFloat()+10)*scale
    }
    yMax = 10
    heighMax = 0

    yMin = -10
    heighMin = 2000




}