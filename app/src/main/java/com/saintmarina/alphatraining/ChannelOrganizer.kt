package com.saintmarina.alphatraining

import android.content.Context


const val MIN = -20000.0f
const val MAX = 20000.0f
const val NUM_SECS_ON_SCREEN = 10
const val SCREEN_WAVE_SAMPLE_RATE_MILLIS = 4 // Eventually, we'll sample and set to >10
const val NUM_POINTS_ON_SCREEN = NUM_SECS_ON_SCREEN*1000/SCREEN_WAVE_SAMPLE_RATE_MILLIS

data class ChannelOrganizer(val context: Context) {
    val vizDataAll = DoubleCircularArray(NUM_POINTS_ON_SCREEN)
    val vizDataAlpha = DoubleCircularArray(NUM_POINTS_ON_SCREEN)
    val vizDataEnvelope = DoubleCircularArray(NUM_POINTS_ON_SCREEN)

    val visualizer = WaveVisualizer(context).apply {
        values = vizDataAll
        yMin = MIN
        yMax = MAX
    }

    val fAll = CascadedBiquadFilter(FilterCoefficients.allWaves)
    val fAlpha = CascadedBiquadFilter(FilterCoefficients.alphaWaves)
    val fEnvelope = CascadedBiquadFilter(FilterCoefficients.envelopeDetection)

    fun pushValue(v: Double) {
        vizDataAll.push(fAll.filter(v))
        vizDataAlpha.push(fAlpha.filter(v))
        vizDataEnvelope.push(fEnvelope.filter(v))
    }

    fun showAllWaves() {
        visualizer.values = vizDataAll
        visualizer.yMin = MIN
        visualizer.yMax = MAX
    }
    fun showAlphaWaves() {
        visualizer.values = vizDataAlpha
        visualizer.yMin = -50.0f
        visualizer.yMax = 50.0f
    }
    fun showEnvelopeWaves() {
        visualizer.values = vizDataEnvelope
        visualizer.yMin = MIN
        visualizer.yMax = MAX
    }
}