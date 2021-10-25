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
import android.view.ViewGroup
import android.widget.LinearLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// TODO create a piece of fake data and show it on the screen
// * create an array of DoubleCircularArray with values 10 to -10
// TODO create Circular array for each type of wave (normal, alpha, envelope)
// TODO feed the Circular array with some data from fresh brain data in real time
// TODO have a timer(interval 15 milliseconds) this is where i redraw the graphs
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)
        //Creating DoubleCircularArray
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



        val visualizer1 = WaveVisualizer(this, sampleArray,numOfWaves*nums.size, 10.0)
        linearLayout.addView(visualizer1)
        visualizer1.invalidate()
       // WaveVisualizer.hello = 123


        /*This code will make the wave animated
        linearLayout.addView(visualizer1)
        Observable.interval(15, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {

                visualizer1.invalidate()
            }*/


/*7*/
    }
}


