package com.saintmarina.alphatraining

import android.content.Context
import android.media.MediaPlayer
import kotlin.math.pow

class Audio(var context: Context) {
    var player:MediaPlayer? = null

    fun play() {
        player = MediaPlayer.create(context, R.raw.sound)
        player!!.isLooping = true
        player!!.start()
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
    }

    fun setVolume(volume_: Float){
        var volume = volume_
        val baseline = 0.15f;
        volume -= baseline
        if (volume < 0.01f) {
            volume = 0.01f
        }
        volume *= 1/(1-baseline)
        volume = 2*volume.pow(2)

        player?.setVolume(volume, volume)
    }
}