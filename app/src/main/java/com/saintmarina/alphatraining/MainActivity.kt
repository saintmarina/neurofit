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
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import io.reactivex.rxjava3.schedulers.Schedulers
import org.xmlpull.v1.XmlPullParser

const val CHANNELS = 8

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)

        var channel1_viz = findViewById<WaveVisualizer>(R.id.channel1)

        val channels = Array(8) { ChannelOrganizer() }

        channel1_viz.values = channels[0].vizData


        /*

        for (c in 0 until CHANNELS) {
            linearLayout.addView(
                channels[c].visualizer,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200)
            )
        }
         */

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
                    val ch = channels[c]
                    var v = packet.channels[c];
                    ch.pushValue(v) // TODO finish this function. Code below should go inside pushvalue()
                    //var v = Random.nextDouble(0.0, 100000.0);
                    v = ch.fAlpha.filter(v);
                    // There's a special subscriber that is also an observer.
                    ch.vizData.push(v)
                }

                //normalChanel1.push(f.filter(packet.channels[0])) Left for reference
            }

/*


        var alpha = DoubleCircularArray(250*10)
        var envelope = DoubleCircularArray(250*10)
        val device = OpenBCI(this)
        val observable = device.createPacketStreamObservable()
        observable.subscribe{ packet->
            normalChannel1.push(packet.channels[0])
        }
        val visualizer1 = WaveVisualizer(this, normalChannel1,-50.0, 50.0)
        visualizer1.layoutParams = LinearLayout.LayoutParams(2000, 300) // Here put your custom size
        visualizer1.setBackgroundColor(Color.CYAN)
        linearLayout.addView(visualizer1)
        Observable.interval(15, TimeUnit.MILLISECONDS)
            //.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.d("roses1", "inside the Observable.interval")
                visualizer1.invalidate()

            }

       /* linearLayout.addView(visualizer1)
        visualizer1.invalidate()
*/


    }*/}
}
/*
        //Creating fake data
        val nums = doubleArrayOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
            10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0,
            -1.0, -2.0, -3.0, -4.0, -5.0, -6.0, -7.0, -8.0, -9.0, -10.0, -9.0, -8.0, -7.0, -6.0, -5.0, -4.0, -3.0, -2.0, -1.0)
        val numOfWaves = 10
        val sampleArray = DoubleCircularArray(numOfWaves*nums.size)
        for (i in 0..numOfWaves) {
            for (j in nums.indices) {
               sampleArray.push(nums[j])
            }
        }
 */



