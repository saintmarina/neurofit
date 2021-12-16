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

// TODO what about an idea of making a separate class for channels array
// TODO disable screen rotation
// TODO UI: make the channels look more scientifiky
// * every second have a light gray line dividing the channel
// * on the very top put numbers (for seconds) give a white contour(outline)
// FIXME UI FREEZING BUG:
// * get rid of runOnUiThread. Make instance variables that will be set everytime we push a packet.
// * Have separate RxJava "observable.schedule on Main Thread wiht an interval", there update instance variables
// TODO take out 0, 50, 100 buttons
// TODO SEEKBAR:
// * make it shorter (add right and left margins)
// * Add a TOGGLE BUTTON autoscale. When on the seekbar is disabled, and the yMax is the max of all channel maxes combined ( of what is being displayed Allwaves/AlphaWaves/ etc.)
// * When off the  seekbar is enabled and it pick the yMax = 300*100/(seekbar.progress)
// * the volume should always be the envelopeValue/yMax
// TODO TOGGLE BUTTON:
// * the button would be green, once pressed it would turn red
// * the button would indicate start/stop training (toggle the text START/STOP)
// * if the button is pressed the user would start hearing Alpha Waves
// * else there would be so sound
// TODO count the score of the session
// * calculation for the score:  average of the volume(alpha waves)
// * have a DoubleCircularArray for each score
// * have a score for the LAST 1 minute (for calibrating) all the time
// * LAST 10 minutes
// * WHOLE SESSION
// TODO add a timer to time the session
// FIXME prevent the app from crashing when the OpenBCI is unplugged
// * Display a dialogue box and
// * if in the middle of a training session, save all the relevant data to be able to continue
// TODO think about 5-6 seconds alpha wave bursts combos


import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)
        val buttonAll = findViewById<Button>(R.id.button_all_waves)
        val buttonAlpha = findViewById<Button>(R.id.button_alpha_waves)
        val buttonEnvelope = findViewById<Button>(R.id.button_envelope_waves)

        val textMax = findViewById<TextView>(R.id.textMax)
        val textVolume = findViewById<TextView>(R.id.textVolume)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)


        val player = Audio(this).apply { play() }
        val channels = Channels(this)

        buttonAll.setOnClickListener {
            channels.channels.forEach { c -> c.showAllWaves() }
        }
        buttonAlpha.setOnClickListener {
            channels.channels.forEach { c -> c.showAlphaWaves() }
        }
        buttonEnvelope.setOnClickListener {
            channels.channels.forEach { c -> c.showAlphaEnvelopeWaves() }
        }

        channels.addToLayout(linearLayout)

        // Populating data IRL
        OpenBCI(this)
            .createPacketStreamObservable()
            .subscribeOn(Schedulers.newThread())
            .map { packet ->
                channels.pushValueInEachChannel(packet)
                channels.updateMinMaxVisualizers()
                player.setVolume(channels.computeVolume(seekBar.progress))
            }
            .sample(100, TimeUnit.MILLISECONDS)
            .subscribe { volume ->
                // .observeOn(AndroidSchedulers.mainThread()) doesn't work for some reason
                // using runOnUiThread as a workaround
                runOnUiThread {
                    textMax.text = channels.yMaxOfAllChannels.toInt().toString()
                    textVolume.text = "${(volume * 100).toInt()}%"
                }
            }
    }
}


