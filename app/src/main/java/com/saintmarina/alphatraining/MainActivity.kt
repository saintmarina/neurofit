package com.saintmarina.alphatraining
/* Set up WiFi debuging
 * Connect the Tablet to the Laptop via USB-C cable
 * In terminal:
 * adb shell setprop service.adb.tcp.port # probably not needed
 * adb tcpip 4444
 * adb connect 192.168.0.219:4444
 * Disconnect USB cable.
 * Done.
 * In the Logcat: make sure to choose correct device. There should be two devices: one that is
 * connected via USB (should say [DISCONECTED]) and the other via WiFi. You are interested in the
 * one that doesn't have the [DISCONECTED] note nest to it.
 * Good luck!
*/

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

const val CHANNELS = 8

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)
        val buttonAll = findViewById<Button>(R.id.button_all_waves)
        val buttonAlpha = findViewById<Button>(R.id.button_alpha_waves)
        val buttonEnvelope = findViewById<Button>(R.id.button_envelope_waves)

        val buttonVolume0 = findViewById<Button>(R.id.button_volume_check_0)
        val buttonVolume50 = findViewById<Button>(R.id.button_volume_check_50)
        val buttonVolume100 = findViewById<Button>(R.id.button_volume_check_100)

        val textMax = findViewById<TextView>(R.id.textMax)
        val textVolume = findViewById<TextView>(R.id.textVolume)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)

        val player = Audio(this).apply { play() }

        buttonVolume0.setOnClickListener {
            player.setVolume(0.0f)
        }
        buttonVolume50.setOnClickListener {
            player.setVolume(0.5f)
        }
        buttonVolume100.setOnClickListener {
            player.setVolume(1.0f)
        }


        val channels = Array(8) { ChannelOrganizer(this) } //TODO what about an idea of making a separate class for this

        buttonAll.setOnClickListener {
            channels.forEach { c -> c.showAllWaves() }
        }
        buttonAlpha.setOnClickListener {
            channels.forEach { c -> c.showAlphaWaves() }
        }
        buttonEnvelope.setOnClickListener {
            channels.forEach { c -> c.showAlphaEnvelopeWaves() }
        }

        channels.forEach { c -> linearLayout.addView(
            c.visualizer,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200))
        }

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
        }

        fun computeVolume(): Float {
            var envelopeAverage = 0.00
            for (i in 0 until CHANNELS) {
                envelopeAverage += channels[i].alphaEnvelopeV
            }
            envelopeAverage /= CHANNELS
            return (envelopeAverage/(seekBar.progress+1)).toFloat()
        }

        // Populating data IRL
        OpenBCI(this)
            .createPacketStreamObservable()
            .subscribeOn(Schedulers.newThread())
            .map { packet ->
                for (i in 0 until CHANNELS) {
                    channels[i].pushValue(packet.channels[i])
                }
                updateMinMaxVisualizers()
                val volume = computeVolume()
                player.setVolume(volume)
                volume
            }
            .sample(100, TimeUnit.MILLISECONDS)
            .subscribe { volume ->
                val yMax = channels[0].visualizer.yMax // they are all the same!!!
                // .observeOn(AndroidSchedulers.mainThread()) doesn't work for some reason
                // using runOnUiThread as a workaround
                runOnUiThread {
                    textMax.text = yMax.toInt().toString()
                    textVolume.text = "${(volume * 100).toInt()}%"
                }
            }
    }
}


