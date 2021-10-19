package com.saintmarina.alphatraining
/* Set up WiFi debuging
 * Connect the Tablet to the Laptop via USB-C cable
 * In terminal:
 * adb shell setprop service.adb.tcp.port
 * adb tcpip 4444
 * adb connect 192.168.0.219:4444
 * Disconnect USB cable.
 * Done.
 * In the Logcat: make sure to choose correct device. There should be two devices: one that is
 * connected via USB (should say [DISCONECTED]) and the other via WiFi. You are interested in the
 * one that doesn't have the [DISCONECTED] note nest to it.
 * Good luck!
*/

// TODO Draw a line on the screen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Prepare the device
        val device = OpenBCI(this)

        // Init all filters
        val allWavesFilter = CascadedBiquadFilter(FilterCoefficients.allWaves)
        val alphaFilter = CascadedBiquadFilter(FilterCoefficients.alphaWaves)
        val envelopeFilter = CascadedBiquadFilter(FilterCoefficients.envelopeDetection)

        device.createPacketStreamObservable()
            .toFlowable(BackpressureStrategy.DROP)
            .observeOn(Schedulers.newThread())
            .subscribeOn(Schedulers.newThread())
            .subscribe({ packet ->
                val sample = packet.channels[0]
                val normal = allWavesFilter.filter(sample)
                val alpha = alphaFilter.filter(sample)
                val envelope = envelopeFilter.filter(abs(alpha))
                Log.i("DATA", "sample = $sample, normal = $normal, alpha = $alpha, envelope = $envelope")
            }, { e->
                Log.e(
                    "ERROR",
                    "OpenBCI device readPacket() failed: ${e.stackTraceToString()}"
                )
            })
    }
}


