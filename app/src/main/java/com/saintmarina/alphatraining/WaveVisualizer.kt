package com.saintmarina.alphatraining

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withMatrix

class WaveVisualizer(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    var values: DoubleCircularArray = DoubleCircularArray(NUM_POINTS_ON_SCREEN)
        set(value) {
            field = value
            points = FloatArray((values.size - 1) * 4)
        }
    private var points = FloatArray((values.size - 1) * 4) // TODO re-initialize when values is set

    var yMin = attributeSet.getAttributeFloatValue(R.attr.yMin, -1.0f).toDouble()
    var yMax = attributeSet.getAttributeFloatValue(R.attr.yMax, 1.0f).toDouble()

    private val m = Matrix()
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

        m.reset()
        m.preTranslate(0.0f, -yMin.toFloat())
        m.postScale(width / (-n + 1), (height / (yMin - yMax)).toFloat())
        m.postTranslate(width, height)

        canvas!!.withMatrix(m) {
            drawLines(points, paint)
        }

        // redraw all the time
        invalidate() // TODO look later if we need this line. Perhaps we should invalidate only
                    //  when we update values
    }
}