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
import android.app.ActionBar
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.Slide
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import kotlin.math.pow

private const val REQUEST = 112
@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private var isRecording = false
    private var volume: Float = 0.0f
    private var alphaAmplitude: Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        initRxErrorHandler()

        val vizLayout = findViewById<LinearLayout>(R.id.visualizerFullLayout)
        val containerLayout = findViewById<LinearLayout>(R.id.vizContainerLayout)
        val radioGroup = findViewById<RadioGroup>(R.id.radioWaves)

        val buttonStartStop = findViewById<ToggleButton>(R.id.start_stop_toggle_button)
           // .apply { setBackgroundColor(Color.GREEN) }
        val buttonStartStopRecording = findViewById<ToggleButton>(R.id.start_stop_recording)

        val buttonReplay = findViewById<ToggleButton>(R.id.replay)
        val buttonRealTime = findViewById<ToggleButton>(R.id.real_time)

        val seekBar = findViewById<SeekBar>(R.id.seekBar).apply { progress = 300 }
        val buttonAutoScale = findViewById<ToggleButton>(R.id.button_autoscale)

        val textMax = findViewById<TextView>(R.id.textMax)
        val textVolume = findViewById<TextView>(R.id.textVolume)

        val player = Audio(this)
        val visColorArray = arrayOf(Color.parseColor("#C20000"), Color.parseColor("#E6701C"), Color.parseColor("#BC9700"), Color.parseColor("#038A0C"), Color.BLUE, Color.parseColor("#800080"), Color.BLACK, Color.GRAY,)
        val channels = Channels(Array(8) { i -> ChannelOrganizer(this, visColorArray[i]) })
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioAllWaves -> channels.channels.forEach { c -> c.showAllWaves() }
                R.id.radioAlphaWaves -> channels.channels.forEach { c -> c.showAlphaWaves() }
                R.id.radioEnvWaves -> channels.channels.forEach { c -> c.showAlphaEnvelopeWaves() }
            }
        }

        var brainFileWriter: BrainFile.Writer? = null
        buttonStartStop.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //buttonStartStop.setBackgroundColor(Color.RED)
                player.play()

            } else {
                //buttonStartStop.setBackgroundColor(Color.GREEN)
                player.stop()
            }
        }

        buttonStartStopRecording.setOnCheckedChangeListener { _, isChecked ->
            isRecording = isChecked
        }

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

        val a = arrayOf("O1", "O2", "P3", "P4", "C3", "C4", "Fp1", "Fp2")
        a.forEach { s -> vizLayout.addView(
            TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 4f)
                text = s
                gravity = 0x11
                setTextColor(Color.LTGRAY)
            })
        }
        vizLayout.addView(TextView(this).apply { layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f) })

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
                    val file = File(BrainFile.commonDocumentDirPath(this), BrainFile.getDefaultFileName())
                    brainFileWriter = BrainFile().Writer(file)
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

        fun updateMusicVolume() {
            val isEnv = radioGroup.checkedRadioButtonId == R.id.radioEnvWaves
            if (buttonAutoScale.isChecked)
                channels.autoscale()
            else
                channels.setScale(25*(seekBar.progress/seekBar.max.toFloat()).pow(1.5f), isEnv)

            alphaAmplitude = channels.computeAlphaWaveAmplitude()
            volume =  alphaAmplitude.toFloat()/(channels.limit+0.00001F) // Here adding 1 so we never divide by 0
            if (buttonStartStop.isChecked)
                player.setVolume(volume)
        }

        fun processPacket(packet: OpenBCI.Packet) {
            maybeWriteBrainData(packet)
            channels.pushValueInEachChannel(packet)
            channels.updateAllVizualizers()
            updateMusicVolume()
        }

        val subscribePacketProcessor = run {
            var packetStream: Disposable? = null

            { enable: Boolean, src: () -> Observable<OpenBCI.Packet> ->
                packetStream?.dispose()
                packetStream = if (enable) {
                    try {
                        src().subscribeOn(Schedulers.newThread())
                            .subscribe { packet -> processPacket(packet) }
                    } catch (e: RuntimeException) {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT)
                            .show()
                        null
                    }
                } else {
                    null

                }
                packetStream != null
            }
        }

        buttonRealTime.setOnCheckedChangeListener { b, isChecked ->
            b.isChecked = subscribePacketProcessor(isChecked) {
                OpenBCI(this).createPacketStreamObservable()
            }
            buttonReplay.isEnabled = !b.isChecked
        }

        buttonReplay.setOnCheckedChangeListener { b, isChecked ->
            b.isChecked = subscribePacketProcessor(isChecked) {
                val file = BrainFile.getLastRecordedFile(this)
                BrainFile().Reader(file).createPacketStreamObservable()
            }
            buttonRealTime.isEnabled = !b.isChecked
        }

        Observable.interval(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                textMax.text = "AlphaWave = %.2f µV\n limit = %.2f µV".format(alphaAmplitude, channels.limit)
                textVolume.text = "volume = ${(volume * 100).toInt()}%"
                channels.updateAllVizualizers()
            }
        updateMusicVolume()
    }

    /* This catches the Undeliverable exception generated by RX Java,
     * when REPLAY button is toggled too fast
     */
    private fun initRxErrorHandler() {
        RxJavaPlugins.setErrorHandler { throwable ->
            throwable.message?.let { Log.e("Undeliverable exception", it) }
        }
    }
/*
 * This piece of code requests permissions to WRITE and READ external storage.
 * Right now the app is saving and replaying brain data from internal storage,
 * thus we don't need to request these permissions. To change the dir of where the files
 * are saved goto BrainFile.kt -> companion object -> commonDocumentDirPath and uncomment
 * the code that is under the return statement

    private fun checkPermissionsForRecording() {
         if (Build.VERSION.SDK_INT >= 23) {
           val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
           if (!hasPermissions(permissions)) {
               ActivityCompat.requestPermissions((this as Activity), permissions, REQUEST)
           } else {
               isRecording = true
           }
       }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

                    return false
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
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isRecording = true
                } else {
                    Toast.makeText(this, "The application can't operate without requested permissions. Grant all requested permissions to proceed.", Toast.LENGTH_SHORT)
                        .show()
                    checkPermissionsForRecording()
                }
            }
        }
    }*/


}




