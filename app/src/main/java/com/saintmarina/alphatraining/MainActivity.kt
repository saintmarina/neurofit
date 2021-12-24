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
 * If the Logcat crashed with NullPointerException (doesn't respond) in the terminal type:
 * adb kill-server
 * adb connect 192.168.0.219:4444
 * The Android Studio should have reconnected with the decive.
*/

// TODO SEEKBAR:
// * edit AUTOSCALE button to be a toggle button
// TODO make all the buttons to be <ToggleButton>
// TODO UI: make the channels look more scientific
// * on the very top put numbers (for seconds) give a white contour(outline)
// TODO put all the channelVisualizers into a UI container and they should fill the entire container height and width
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
// TODO think about adding an instructions/tutorial activity
// TODO make each channel of a different color (corresponding to the wire color of the OpenBCI electrodes)

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    var volume: Float = 0.0f
    var isTraining = false
    var isAutoScaling = false
    var isEnvelope = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)
        val buttonAll = findViewById<Button>(R.id.button_all_waves)
        val buttonAlpha = findViewById<Button>(R.id.button_alpha_waves)
        val buttonEnvelope = findViewById<Button>(R.id.button_envelope_waves)
        val buttonStartStop = findViewById<Button>(R.id.start_stop_toggle_button)
            .apply {
                setBackgroundColor(Color.GREEN)
                text = "START"
            }
        val buttonAutoScale = findViewById<Button>(R.id.button_autoscale)

        val textMax = findViewById<TextView>(R.id.textMax)
        val textVolume = findViewById<TextView>(R.id.textVolume)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        fun refreshAutoScalingUI() {
            if (isAutoScaling) {
                buttonAutoScale.setBackgroundColor(Color.parseColor("#FF6200EE")) // Took it from colors.xml
                seekBar.isEnabled = false
            } else {
                buttonAutoScale.setBackgroundColor(Color.LTGRAY)
                seekBar.isEnabled = true
            }
        }
        refreshAutoScalingUI()

        val player = Audio(this)
        val channels = Channels(Array(8) { ChannelOrganizer(this) })

        buttonAutoScale.setOnClickListener {
            isAutoScaling = !isAutoScaling
            refreshAutoScalingUI()
        }

        buttonStartStop.setOnClickListener {
            isTraining = !isTraining
            if (isTraining) {
                buttonStartStop.text = "STOP"
                buttonStartStop.setBackgroundColor(Color.RED)
                player.play()
            } else {
                buttonStartStop.text = "START"
                buttonStartStop.setBackgroundColor(Color.GREEN)
                player.stop()
            }
        }

        buttonAll.setOnClickListener {
            channels.channels.forEach { c -> c.showAllWaves() }
            isEnvelope = false
        }
        buttonAlpha.setOnClickListener {
            channels.channels.forEach { c -> c.showAlphaWaves() }
            isEnvelope = false
        }
        buttonEnvelope.setOnClickListener {
            channels.channels.forEach { c -> c.showAlphaEnvelopeWaves() }
            isEnvelope = true
        }

        channels.channels.forEach { c -> linearLayout.addView(
            c.visualizer,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200))
        }

        // Populating data IRL
        OpenBCI(this)
            .createPacketStreamObservable()
            .subscribeOn(Schedulers.newThread())
            .subscribe { packet ->
                channels.pushValueInEachChannel(packet)
                if (isAutoScaling)
                    channels.autoscale()
                else
                    channels.setScale(seekBar.progress, isEnvelope)
                volume = channels.computeVolume(seekBar.progress)
                if (isTraining)
                    player.setVolume(volume)
            }

        Observable.interval(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                textMax.text = "yMax = ${channels.yMaxOfAllChannels.toInt()}"
                textVolume.text = "volume = ${(volume * 100).toInt()}%"
            }
    }
}


