package com.saintmarina.alphatraining

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class GridOfSeconds(context: Context) : View(context) {
    private val fontSize = 30F
    private val textArray = Array(NUM_SECS_ON_SCREEN) { i -> (NUM_SECS_ON_SCREEN - i).toString()}
    private val paint = Paint().apply {
        color = Color.LTGRAY
        textSize = fontSize
        strokeWidth = 0F
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.apply {
            for ((i, text) in textArray.withIndex()) {
                if (i == 0)
                    continue
                drawText(text, i*width/NUM_SECS_ON_SCREEN - fontSize/3, fontSize, paint)
            }
        }
    }
}