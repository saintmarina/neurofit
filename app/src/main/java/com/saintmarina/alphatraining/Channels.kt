package com.saintmarina.alphatraining

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlin.math.max
import kotlin.math.min

const val CHANNELS = 8

class Channels(context: Context) {
    val channels = Array(8) { ChannelOrganizer(context) }
    var yMaxOfAllChannels = channels[0].visualizer.yMax // they are all the same!!!


    fun updateMinMaxVisualizers() {
        var yMax = 1.0f
        var yMin = 0.0f
        for (i in 0 until CHANNELS) {
            yMax = max(channels[i].maxPoint, yMax)
            yMin = min(channels[i].minPoint, yMin)
        }
        channels.forEach { c ->
            c.visualizer.yMax = yMax
            c.visualizer.yMin = yMin
        }
        yMaxOfAllChannels = channels[0].visualizer.yMax
    }

    fun computeVolume(progress: Int): Float {
        var envelopeAverage = 0.00
        for (i in 0 until CHANNELS) {
            envelopeAverage += channels[i].alphaEnvelopeV
        }
        envelopeAverage /= CHANNELS
        return (envelopeAverage/(progress+1)).toFloat() // Here adding 1 so we never divide by 0
    }

    fun addToLayout(linearLayout: LinearLayout) {
        channels.forEach { c -> linearLayout.addView(
            c.visualizer,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200))
        }
    }

    fun pushValueInEachChannel(packet: OpenBCI.Packet) {
        for (i in 0 until CHANNELS) {
            channels[i].pushValue(packet.channels[i])
        }
    }
}