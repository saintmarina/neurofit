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

    private val fAll = CascadedBiquadFilter(FilterCoefficients.allWaves)
    private val fAlpha = CascadedBiquadFilter(FilterCoefficients.alphaWaves)
    private val fAlphaEnvelope = CascadedBiquadFilter(FilterCoefficients.envelopeDetection)

    fun pushValue(v: Double) {
        if (counter == 0) {
            // TODO take out the filter() calls outside of the if/else
            fAll.filter(v)
            fAlpha.filter(v)
            fAlphaEnvelope.filter(v)
            counter = 2
        } else {
            vizDataAll.push(fAll.filter(v))
            setNewAmplitude(visualizer)
            val alphaV = fAlpha.filter(v)
            vizDataAlpha.push(alphaV)
            // The output of the envelope filter will be the volume of the audio
            vizDataAlphaEnvelope.push(fAlphaEnvelope.filter(abs(alphaV)))
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

    fun setNewAmplitude(visualizer: WaveVisualizer) {
        // TODO all visualizers should have the same min/max settings
        val newMax = max(visualizer.values.array.maxOrNull() ?: 1.00,
            abs(visualizer.values.array.minOrNull()?: 1.00) )
        val newMin = if (visualizer.values == vizDataAlphaEnvelope) 0.00 else -newMax
        visualizer.yMax = newMax.toFloat()
        visualizer.yMin = newMin.toFloat()
    }

}