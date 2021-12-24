package com.saintmarina.alphatraining

import android.content.Context
import android.media.MediaPlayer


class Audio(var context: Context) {
    var player:MediaPlayer? = null

    fun play() {
        player = MediaPlayer.create(context, R.raw.mixed_binaural_beat_10m)
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

        if (volume < 0.01f) {
            volume = 0.01f
        }
        player?.setVolume(volume, volume)
    }
}