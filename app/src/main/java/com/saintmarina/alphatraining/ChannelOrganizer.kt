package com.saintmarina.alphatraining

import android.content.Context


const val MIN: Double = -20000.0
const val MAX: Double = 20000.0
const val NUM_SECS_ON_SCREEN = 10
const val SCREEN_WAVE_SAMPLE_RATE_MILLIS = 4 // Eventually, we'll sample and set to >10
const val NUM_POINTS_ON_SCREEN = NUM_SECS_ON_SCREEN*1000/SCREEN_WAVE_SAMPLE_RATE_MILLIS

// TODO add 3 buttons on the screen to choose which DATA to show on the screen
data class ChannelOrganizer(val context: Context) {
    var counterPushData = 0
    val vizData = DoubleCircularArray(NUM_POINTS_ON_SCREEN) // TODO add 3 different arrays for 3 filters
    val visualizer = WaveVisualizer(context, vizData, MIN, MAX)

    val fAll = CascadedBiquadFilter(FilterCoefficients.allWaves)
    val fAlpha = CascadedBiquadFilter(FilterCoefficients.alphaWaves)
    val fEnvelope = CascadedBiquadFilter(FilterCoefficients.envelopeDetection)

    fun pushValue(v: Float) {

    }
}