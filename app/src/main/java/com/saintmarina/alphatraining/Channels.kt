package com.saintmarina.alphatraining

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

const val CHANNELS = 8

class Channels(val channels: Array<ChannelOrganizer>) {
    var limit = 0F

    fun autoscale() {
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
        limit = yMax
    }

    fun setScale(sensitivity: Float, envelopeWave: Boolean) {
        val yMax = sensitivity
        channels.forEach { c ->
            c.visualizer.yMax = yMax
            c.visualizer.yMin = if (envelopeWave) 0F else -yMax
        }
        limit = yMax
    }

    fun computeAlphaWaveAmplitude(): Float {
        val amplitude = channels.map { c -> c.alphaEnvelopeV }.maxOrNull()
        return amplitude!!.toFloat()
    }

    fun pushValueInEachChannel(packet: OpenBCI.Packet) {
        for (i in 0 until CHANNELS) {
            channels[i].pushValue(packet.channels[i])
            //bytearray.pushFloat(packet.channels[i])
        }
        //file.write(bytearray)
    }

    fun updateAllVizualizers() {
        channels.forEach { c -> c.updateVisualizer() }
    }
}