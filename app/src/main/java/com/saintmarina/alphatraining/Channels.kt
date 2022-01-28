package com.saintmarina.alphatraining

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

const val CHANNELS = 8

class Channels(val channels: Array<ChannelOrganizer>) {
    var yMaxOfAllChannels = 0F

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
        yMaxOfAllChannels = channels[0].visualizer.yMax
    }

    fun setScale(sensitivity: Int, envelopeWave: Boolean) {
        val yMax = 1000*(sensitivity.toFloat()/1000).pow(3F)
        channels.forEach { c ->
            c.visualizer.yMax = yMax
            c.visualizer.yMin = if (envelopeWave) 0F else -yMax
        }
        yMaxOfAllChannels = yMax
    }

    fun computeVolume(progress: Int): Float {
        var envelopeAverage = 0.00
        for (i in 0 until CHANNELS) {
            envelopeAverage += channels[i].alphaEnvelopeV
        }
        envelopeAverage /= CHANNELS
        return (envelopeAverage/(progress+1)).toFloat() // Here adding 1 so we never divide by 0
    }

    fun pushValueInEachChannel(packet: OpenBCI.Packet) {
        for (i in 0 until CHANNELS) {
            channels[i].pushValue(packet.channels[i])
            //bytearray.pushFloat(packet.channels[i])
        }
        //file.write(bytearray)
    }
}