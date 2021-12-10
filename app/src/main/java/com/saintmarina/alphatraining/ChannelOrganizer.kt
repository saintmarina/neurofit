package com.saintmarina.alphatraining

import android.content.Context
import kotlin.math.abs
import kotlin.math.max


const val MIN = -20000.0f
const val MAX = 20000.0f
const val NUM_SECS_ON_SCREEN = 10
const val SCREEN_WAVE_SAMPLE_RATE_MILLIS = 4 // Eventually, we'll sample and set to >10
const val NUM_POINTS_ON_SCREEN = NUM_SECS_ON_SCREEN*1000/SCREEN_WAVE_SAMPLE_RATE_MILLIS

data class ChannelOrganizer(val context: Context) {
    private var counter = 2

    private val vizDataAll = DoubleCircularArray(NUM_POINTS_ON_SCREEN)
    private val vizDataAlpha = DoubleCircularArray(NUM_POINTS_ON_SCREEN)
    private val vizDataAlphaEnvelope = DoubleCircularArray(NUM_POINTS_ON_SCREEN)

    val visualizer = WaveVisualizer(context).apply {
        values = vizDataAll
        yMin = MIN
        yMax = MAX
    }

    var maxPoint = 1.0f
    var minPoint = 0.0f

    private val fAll = CascadedBiquadFilter(FilterCoefficients.allWaves)
    private val fAlpha = CascadedBiquadFilter(FilterCoefficients.alphaWaves)
    private val fAlphaEnvelope = CascadedBiquadFilter(FilterCoefficients.envelopeDetection)

    fun pushValue(v: Double) {
        val allV = fAll.filter(v)
        val alphaV = fAlpha.filter(v)
        val alphaEnvelopeV = fAlphaEnvelope.filter(abs(alphaV))
        if (counter == 0) {
            counter = 2
        } else {
            vizDataAll.push(allV)
            vizDataAlpha.push(alphaV)
            vizDataAlphaEnvelope.push(alphaEnvelopeV)
            setNewAmplitude(visualizer)
            counter--
        }
    }

    fun showAllWaves() {
        visualizer.values = vizDataAll
        setNewAmplitude(visualizer)
    }
    fun showAlphaWaves() {
        visualizer.values = vizDataAlpha
        setNewAmplitude(visualizer)
    }
    fun showAlphaEnvelopeWaves() {
        visualizer.values = vizDataAlphaEnvelope
        setNewAmplitude(visualizer)
    }

    private fun setNewAmplitude(visualizer: WaveVisualizer) {
        maxPoint = max(visualizer.values.array.maxOrNull() ?: 1.00,
            abs(visualizer.values.array.minOrNull()?: 1.00) ).toFloat()
        minPoint = if (visualizer.values == vizDataAlphaEnvelope) 0.0f else -maxPoint
    }
}