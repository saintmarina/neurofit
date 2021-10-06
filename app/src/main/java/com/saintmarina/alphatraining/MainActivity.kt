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

// TODO Display brainwave real time values on the screen.

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.Schedulers.io
import io.reactivex.rxjava3.core.SingleEmitter

import io.reactivex.rxjava3.core.SingleOnSubscribe

import io.reactivex.rxjava3.core.Single




class MainActivity : AppCompatActivity() {
    lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //text = findViewById(R.id.text_view)

        // Prepare the device
        val device = OpenBCI(this)
        device.waitForDevice()
        device.startStreaming()

        // Show the values on the screen
        // Create a single observable of type Packet
        val packetObservable = getSingleObservable(device)
        val singleObserver = getSingleObserver()

        packetObservable
            .observeOn(io())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(singleObserver)

       /* while(true) {
            val packet = device.readPacket()
            // TODO read 10 packets, and log each of them
            Log.i(
                "INFO",
                "sample = ${packet.sampleNumber}, channels = ${packet.channels.joinToString(", ")}"
            )
        }*/
    }

    private fun getSingleObservable(device: OpenBCI): Single<OpenBCI.Packet> {
        Log.i("INFO", "creating single")
        return Single.create { emitter ->

            val packet = device.readPacket()
            emitter.onSuccess(packet)
            emitter.onSuccess(packet)
        }
    }

    private fun getSingleObserver(): SingleObserver<OpenBCI.Packet> {
        return object: SingleObserver<OpenBCI.Packet> {
            override fun onSubscribe(d: Disposable) {
                Log.i("Main", "onSubscribe")
                disposable = d
            }

            override fun onSuccess(t: OpenBCI.Packet) {
                Log.i("Main1", "onSuccess: sample = ${t.sampleNumber}, channels = ${t.channels.joinToString(", ")}")
            }

            override fun onError(e: Throwable) {
                Log.e("Main2", "onError: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}


