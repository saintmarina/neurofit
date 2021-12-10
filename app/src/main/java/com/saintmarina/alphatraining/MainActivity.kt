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

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.math.max
import kotlin.math.min

const val CHANNELS = 8

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)
        val buttonAll = findViewById<Button>(R.id.button_all_waves)
        val buttonAlpha = findViewById<Button>(R.id.button_alpha_waves)
        val buttonEnvelope = findViewById<Button>(R.id.button_envelope_waves)
        val text = findViewById<TextView>(R.id.text)

        val channels = Array(8) { ChannelOrganizer(this) }

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

            runOnUiThread { text.text = yMax.toInt().toString() }
        }

        // Populating data IRL
        OpenBCI(this)
            .createPacketStreamObservable()
            .subscribeOn(Schedulers.newThread())
            .subscribe { packet ->
                for (i in 0 until CHANNELS) {
                    channels[i].pushValue(packet.channels[i])
                }
                updateMinMaxVisualizers()
            }
    }
}


