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
import io.reactivex.rxjava3.schedulers.Schedulers

const val CHANNELS = 8

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)
        val buttonAll = findViewById<Button>(R.id.button_all_waves)
        val buttonAlpha = findViewById<Button>(R.id.button_alpha_waves)
        val buttonEnvelope = findViewById<Button>(R.id.button_envelope_waves)


        val channels = Array(8) { ChannelOrganizer(this) }
        buttonAll.setOnClickListener {
            for (c in 0 until CHANNELS) {
                channels[c].showAllWaves()
            }
        }
        buttonAlpha.setOnClickListener {
            for (c in 0 until CHANNELS) {
                channels[c].showAlphaWaves()
            }
        }
        buttonEnvelope.setOnClickListener {
            for (c in 0 until CHANNELS) {
                channels[c].showEnvelopeWaves()
            }
        }
        for (c in 0 until CHANNELS) {
            linearLayout.addView(
                channels[c].visualizer,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200)
            )
        }

        /*
        Observable.interval(15, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                for (c in 0 until CHANNELS) {
                    allWaves.visualizers[c].invalidate()
                }
            }
        */

        // Populating data IRL
        val device = OpenBCI(this)
        device.createPacketStreamObservable()
            //Observable.interval(30, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.newThread())
            // .sample(SCREEN_WAVE_SAMPLE_RATE_MILLIS.toLong(), TimeUnit.MILLISECONDS) // Eventually, we'll sample to speed up display
            .subscribe { packet ->
                //var packet = packets[2]
                for (c in 0 until CHANNELS) {
                    val v = packet.channels[c]
                    //var v = Random.nextDouble(0.0, 100000.0);
                    channels[c].pushValue(v)
                }
            }
    }
}


