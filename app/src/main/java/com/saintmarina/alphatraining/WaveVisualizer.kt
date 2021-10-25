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

// yLim
class WaveVisualizer(context: Context, var values: DoubleCircularArray,var valuesSize:Int, yLim: Double): View(context) {
    companion object {
        var hello = 0
    }
    var screenWidth = 0
    var screenHeight = 0
    var variable = 0
    private val paint = Paint().also { it.color = Color.BLACK; it.strokeWidth = 5F; it.style = Paint.Style.STROKE }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w
        screenHeight = h
        Log.i("Visualizer", "onSizeChanged 1 width = ${screenWidth}, height = ${screenHeight}, values.size = $valuesSize")
       // layoutParams.height = (screenHeight*0.2).roundToInt()
        //layoutParams.width = (screenWidth*0.2).roundToInt()
        Log.i("Visualizer", "onSizeChanged 1 width = ${layoutParams.width}, height = ${layoutParams.height}, values.size = $valuesSize")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Log.i("Visualizer", "onDraw")
        variable += 50
        val starTime = System.nanoTime()
        val scale = 10F

        var j = 0
        val spaceBetweenVertex = 10F

        // The scale will be screenWidth divided the number of points I want to draw on the screen

        var path = Path()
        path.moveTo(screenWidth.toFloat(), (values.getRelativeToLast(0).toFloat()+10)*scale)
        for (i in 1 until valuesSize) {
            path.lineTo(screenWidth.toFloat() - j, (values.getRelativeToLast(-i).toFloat()+10)*scale)
            // You want to fill from the very right to the very left(the newest value would be on the right)
                // Display all the values of the array within the width that I have
                    /*x-->
                      y
                      |
                     */
/*
            canvas?.drawLine(screenWidth.toFloat() - j, values.getRelativeToLast(-(i + 1)).toFloat()*scale,
                screenWidth.toFloat()-spaceBetweenVertex - j, values.getRelativeToLast(-i).toFloat()*scale, paint)*/
            j += 20
        }
        canvas?.drawPath(path, paint)
        val curTime = System.nanoTime()
        Log.i("WaveVisualizer", "duration of the loop = ${(curTime-starTime).toFloat()/1_000_000.0}ms")
    }
}