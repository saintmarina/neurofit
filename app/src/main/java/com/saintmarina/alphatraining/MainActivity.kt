package com.saintmarina.alphatraining
/* Set up WiFi debugging
 * Connect the Tablet to the Laptop via USB-C cable
 * In terminal:
 * adb shell setprop service.adb.tcp.port # probably not needed
 * adb tcpip 4444
 * adb connect 192.168.0.220:4444
 * Disconnect USB cable.
 * Done.
 * IF DOESN'T WORK: check ip address on the tablet About tablet->Status Information->IP Address
 * run adb connect ...new ip address
 * In the Logcat: make sure to choose correct device. There should be two devices: one that is
 * connected via USB (should say [DISCONNECTED]) and the other via WiFi. You are interested in the
 * one that doesn't have the [DISCONNECTED] note nest to it.
 * Good luck!
 * If the Logcat crashed with NullPointerException (doesn't respond) in the terminal type:
 * adb kill-server
 * adb connect 192.168.0.219:4444
 * The Android Studio should have reconnected with the device.
*/

// TODO be able to record a session
// * Try requesting permission to write and read external files at runtime
// * the goal is to save the file into Download folder
// TODO add a timer to time the session
// FIXME prevent the app from crashing when the OpenBCI is unplugged
// TODO count the score of the session
// * calculation for the score:  average of the volume(alpha waves)
// * have a DoubleCircularArray for each score
// * have a score for the LAST 1 minute (for calibrating) all the time
// * LAST 10 minutes
// * WHOLE SESSION
// * Display a dialogue box and
// * if in the middle of a training session, save all the relevant data to be able to continue

// TODO think about 5-6 seconds alpha wave bursts combos
// TODO think about adding an instructions/tutorial activity
// TODO make each channel of a different color (corresponding to the wire color of the OpenBCI electrodes)

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

private const val REQUEST = 112
@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private var isRecording = false
    private var volume: Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       /* if (Build.VERSION.SDK_INT >= 23) {
            val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            if (!hasPermissions(permissions)) {
                ActivityCompat.requestPermissions((this as Activity?)!!, permissions, REQUEST)
            } else {
                Log.i("Tag1", "All permissions granted")
                //do here
            }
        } else {
            Log.i("Tag2", "The OS version is less than what I have")
            //do here
        }*/


        val containerLayout = findViewById<LinearLayout>(R.id.vizContainerLayout)
        val radioGroup = findViewById<RadioGroup>(R.id.radioWaves)

        /***BUTTONS***/
        val buttonStartStop = findViewById<ToggleButton>(R.id.start_stop_toggle_button)
            .apply {
                setBackgroundColor(Color.GREEN)
            }
        val buttonStartStopRecording = findViewById<ToggleButton>(R.id.start_stop_recording)

        val buttonReplay = findViewById<ToggleButton>(R.id.replay)
        /***/

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

        var brainFileWriter: BrainFile.Writer? = null
        /***BUTTONS LOGIC***/
        buttonStartStop.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                buttonStartStop.setBackgroundColor(Color.RED)
                player.play()

            } else {
                buttonStartStop.setBackgroundColor(Color.GREEN)
                player.stop()
            }
        }

        buttonStartStopRecording.setOnCheckedChangeListener { _, isChecked ->
            isRecording = isChecked

        }
/*
        var packetStream = OpenBCI(this).createPacketStreamObservable()
        buttonReplay.setOnCheckedChangeListener { _, isChecked ->
            packetStream = if (!isChecked) {
                OpenBCI(this).createPacketStreamObservable()
            } else {
                file.createPacketStreamObservable()
            }
        }
        var packetStream = if (replay) {
            brainFile("thefile.bd")
                .createPAcketStreamObserveable)
            else {
            openBCI(this)
                .createOAcketStreamObsergable()
        }*/
        /***/

        buttonAutoScale.setOnCheckedChangeListener { _, isChecked ->
            seekBar.isEnabled = !isChecked
        }

        // Fill up the container layout and add it to the parent layout
        channels.channels.forEach { c ->
            containerLayout.addView(
                c.visualizer.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        4f
                    )
                }
            )
        }

        containerLayout.addView(GridOfSeconds(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        })


        fun maybeWriteBrainData(packet: OpenBCI.Packet) {
            when {
                isRecording && brainFileWriter == null -> {
                    brainFileWriter = BrainFile().Writer()
                    brainFileWriter?.writePacket(packet)
                }
                isRecording && brainFileWriter != null -> {
                    brainFileWriter?.writePacket(packet)
                }
                !isRecording && brainFileWriter != null -> {
                    brainFileWriter?.close()
                    brainFileWriter = null

                }
                else  -> {} // Left intentionally blank
            }
        }


        // Populating data IRL
        OpenBCI(this)
            .createPacketStreamObservable()
            .subscribeOn(Schedulers.newThread())
            .subscribe { packet ->
                maybeWriteBrainData(packet)

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
/*
    private fun hasPermissions(permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                    //do here
                } else {
                    Toast.makeText(
                        this,
                        "The app was not allowed to write in your storage",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }*/
//}



