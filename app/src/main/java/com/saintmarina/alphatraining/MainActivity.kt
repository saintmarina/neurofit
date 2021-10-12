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

import io.reactivex.rxjava3.core.Single




class MainActivity : AppCompatActivity() {
    lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //text = findViewById(R.id.text_view)

        // Prepare the device
        val device = OpenBCI(this)
        var counter = 0
        var prevTime = System.nanoTime()

        device.createPacketStreamObservable()
            .toFlowable(BackpressureStrategy.DROP)
            .observeOn(Schedulers.newThread())
            //.subscribeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread())
            //.buffer(10, 1)
            .subscribe({ packet ->
                //Thread.sleep(100)
                counter++
                val curTime = System.nanoTime()
                val duration = curTime - prevTime
                prevTime = curTime

                Log.i(
                    "INFO",
                    "duration = ${duration.toFloat() / 1_000_000}, counter = $counter, sample = ${packet.sampleNumber}, channels = ${
                        packet.channels.joinToString(", ")}")

            }, { e->
                Log.e(
                    "ERROR",
                    "OpenBCI device readPacket() failed: ${e.stackTraceToString()}"
                )
            })
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


