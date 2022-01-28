package com.saintmarina.alphatraining

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import io.reactivex.rxjava3.core.Observable
import java.lang.Exception
import java.nio.ByteBuffer

class OpenBCI(context: Context){
    companion object {
        const val SAMPLE_RATE_HZ = 250
        private const val INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB"
        private const val BUF_CAPACITY = 2000
        private const val PACKET_SIZE = 33
        private const val ADS1299_Vref = 4.5
        private const val ADS1299_gain = 24.0
        private const val eegScale: Double = ADS1299_Vref / ((2 shl 23) - 1).toDouble() / ADS1299_gain * 1000000.0
    }

    private var port: UsbSerialPort
    private var readBuffer = ByteArray(BUF_CAPACITY)
    private var readBufferLength = 0
    private var packetCounter = 0
    private var prevSampleNum = 0

    class Packet(
        var sampleNumber: Int,
        var channels: DoubleArray,
    ) {
        fun fillByteBuf(buf: ByteBuffer) {
            buf.apply {
                putInt(sampleNumber)
                for (ch in channels) {
                    putFloat(ch.toFloat())
                }
            }
        }
    }

    init {
        // Open port the OpenBCI device
        val manager: UsbManager = context.applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
        val availableDrivers: List<UsbSerialDriver> = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            throw RuntimeException("OpenBCI device was not found, try restarting everything")
        }

        val driver: UsbSerialDriver = availableDrivers[0]
        // Check Permissions
        val hasPermission = manager.hasPermission(driver.device)
        if (!hasPermission) {
            // TODO implement proper permission check
            // This permission check doesn't block the main Thread from executing
            // When the app run for the first time, the window requesting permission will pop up
            // The app will crash
            val usbPermissionIntent =
                PendingIntent.getBroadcast(context.applicationContext, 0, Intent(INTENT_ACTION_GRANT_USB), 0)
            manager.requestPermission(driver.device, usbPermissionIntent)
        }

        val connection: UsbDeviceConnection = manager.openDevice(driver.device)
        if (driver.ports.size != 1) {
            throw RuntimeException("USB device ports = ${driver.ports.size}, expected 1. Contact Anna")
        }
        port = driver.ports[0]
        port.open(connection)
        port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
    }

    private fun waitForDevice() {
        // Clearing device's buffer
        Log.i("INFO", "Send reset")
        // This makes the device stop sending data if it was already on and sending data
        sendToDevice('v')
        Log.i("INFO", "discarding any data laying around, including reset header")
        clearDeviceBuffer()

        Log.i("INFO", "sending real reset for header")
        sendToDevice('v')

        Log.i("INFO", "waiting for header to show up")
        waitForHeaders()
    }

    private fun waitForHeaders() {
        fun bufContainsHeaders():Boolean {
            return arrayToAscii(pendingData()).contains(Regex("OpenBCI V3.*\\$\\$\\$", RegexOption.DOT_MATCHES_ALL))
        }

        Log.i("INFO", "wait for device header")
        while (!bufContainsHeaders()) {
            readDataUpTo(readBufferLength+1)
        }
        // clear buffer before returning
        readBufferLength = 0
    }

    private fun startStreaming() {
        Log.i("INFO", "startStreaming: send start streaming command")
        packetCounter = 0
        prevSampleNum = 0
        sendToDevice('b')
    }

    private fun stopStreaming() {
        sendToDevice('s')
    }

    /*
    Packet contents
    Byte 1: 0xA0
    Byte 2: Sample Number
    Bytes 3-5: Data value for EEG channel 1
    Bytes 6-8: Data value for EEG channel 2
    Bytes 9-11: Data value for EEG channel 3
    Bytes 12-14: Data value for EEG channel 4
    Bytes 15-17: Data value for EEG channel 5
    Bytes 18-20: Data value for EEG channel 6
    Bytes 21-23: Data value for EEG channel 6
    Bytes 24-26: Data value for EEG channel 8
    Aux Data Bytes 27-32: 6 bytes of data
    Byte 33: 0xCX where X is 0-F in hex
    */
    private fun readPacket(): Packet {
        fun atTheBeginningOfValidPacket():Boolean {
            // Making sure we have a valid Packet of data of len 33
            // With proper start byte 0xA0 and end byte0xCX where X is 0-F in hex
            return readBuffer[0] == 0xA0.toByte() && 0xC0.toByte().rangeTo(0xCF.toByte()).contains(readBuffer[32])
        }

        fun cast24BitIntTo32BitInt(pos: Int): Int {
            return (readBuffer[pos].toUByte().toInt() shl 16) or
                    (readBuffer[pos+1].toUByte().toInt() shl 8) or
                    readBuffer[pos+2].toUByte().toInt()
        }

        // Step 1: Fill the readBuffer until we have a valid packet
        readDataUpTo(PACKET_SIZE)
        var corruptedPacket = false
        while (!atTheBeginningOfValidPacket()) {
            removeBytesFromFrontReadBuf(1)
            readDataUpTo(PACKET_SIZE)
            corruptedPacket = true
        }
        if (corruptedPacket) {
            Log.w("WARN", "Corrupted data detected. Skipped samples")
        }

        // Step 2: parse the data, as we have the beginning of a valid Packet
        val curSampleNumber = readBuffer[1].toUByte().toInt()
        val channels = DoubleArray(8)

        for (i in 0 until 8) {
            channels[i] = eegScale * cast24BitIntTo32BitInt(2+i*3)
        }

        // Step 3: discard the packet data that we've parsed
        removeBytesFromFrontReadBuf(PACKET_SIZE)

        packetCounter += (curSampleNumber - prevSampleNum + 256) % 256
        prevSampleNum = curSampleNumber
        return Packet(packetCounter, channels)
    }

    fun createPacketStreamObservable(): Observable<Packet> {
        return Observable.create { emitter ->
            try {
                waitForDevice()
                startStreaming()
                while (!emitter.isDisposed) {
                    emitter.onNext(readPacket())
                }
                stopStreaming()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    private fun removeBytesFromFrontReadBuf(n: Int) {
        System.arraycopy(readBuffer, n, readBuffer, 0, readBufferLength - n)
        readBufferLength -= n
    }

    private fun readDataUpTo(size: Int) {
        while (readBufferLength < size) {
            val buf = ByteArray(port.readEndpoint.maxPacketSize)
            val len = port.read(buf,0)
            System.arraycopy(buf, 0, readBuffer, readBufferLength, len)
            readBufferLength += len
        }
    }

    private fun pendingData(): ByteArray {
        return readBuffer.sliceArray(0 until readBufferLength)
    }

    private fun clearDeviceBuffer() {
        val buffer = ByteArray(BUF_CAPACITY)
        while (port.read(buffer, 1000) != 0) {
            // left intentionally blank
        }
    }

    private fun sendToDevice(command: Char) {
        val array = ByteArray(1).also {
            it[0] = command.code.toByte()
        }
        port.write(array, 0)
    }

    private fun arrayToAscii(array: ByteArray): String {
        return array.joinToString("") { i -> if (i <= 0) "." else "%c".format(i) }
    }
}