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
import android.widget.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    var volume: Float = 0.0f
    //var isEnvelope = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)
        val radioGroup = findViewById<RadioGroup>(R.id.radioWaves)
        val buttonStartStop = findViewById<ToggleButton>(R.id.start_stop_toggle_button)
            .apply {
                setBackgroundColor(Color.GREEN)
            }

        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val buttonAutoScale = findViewById<ToggleButton>(R.id.button_autoscale)

        val textMax = findViewById<TextView>(R.id.textMax)
        val textVolume = findViewById<TextView>(R.id.textVolume)

        val player = Audio(this)
        val channels = Channels(Array(8) { ChannelOrganizer(this) })
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioAllWaves -> channels.channels.forEach { c -> c.showAllWaves() }
                R.id.radioAlphaWaves -> channels.channels.forEach { c -> c.showAlphaWaves() }
                R.id.radioEnvWaves -> channels.channels.forEach { c -> c.showAlphaEnvelopeWaves() }
            }
        }

        buttonStartStop.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked) {
                buttonStartStop.setBackgroundColor(Color.RED)
                player.play()
            } else {
                buttonStartStop.setBackgroundColor(Color.GREEN)
                player.stop()
            }
        }

        buttonAutoScale.setOnCheckedChangeListener { _, isChecked ->
            seekBar.isEnabled = !isChecked
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
                val isEnv = radioGroup.checkedRadioButtonId == R.id.radioEnvWaves
                if (buttonAutoScale.isChecked)
                    channels.autoscale()
                else
                    channels.setScale(seekBar.progress, isEnv)
                volume = channels.computeVolume(seekBar.progress)
                if (buttonStartStop.isChecked)
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


