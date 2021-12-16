package com.saintmarina.alphatraining

import android.content.Context
import android.media.MediaParser
import android.media.MediaPlayer
import android.util.Log
import kotlin.math.ln

class Audio(context: Context) {
    val player:MediaPlayer = MediaPlayer.create(context, R.raw.mixed_binaural_beat_10m)

    fun play() {
        player.start()
    }

    fun stop() {
        player.stop()
    }

    fun setVolume(volume_: Float): Float {
        var volume = volume_

        if (volume < 0.05f) {
            volume = 0.05f
        }
        player.setVolume(volume, volume)
        return volume
    }
}