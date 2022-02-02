package com.saintmarina.alphatraining

import android.os.Environment
import io.reactivex.rxjava3.core.Observable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*


private const val FILE_NAME_FMT: String = "yyyy-MM-d HH.mm.ss"

class BrainFile {
    private var byteBuffer: ByteBuffer = ByteBuffer.allocate(36)
    inner class Writer {
        private var outputStream: FileOutputStream = FileOutputStream(File(commonDocumentDirPath(), getBaseName()))

        fun writePacket(packet: OpenBCI.Packet) {
            byteBuffer.position(0)
            packet.fillByteBuf(byteBuffer)
            byteBuffer.position(0)
            outputStream.channel.write(byteBuffer)
        }

        fun close() {
            outputStream.flush()
            outputStream.close()
        }
    }


    inner class Reader {
        private val inputStream = FileInputStream(getLastRecordedFile())

        private fun readPacket(): OpenBCI.Packet? {
            byteBuffer.position(0)
            if (inputStream.channel.read(byteBuffer) == -1)
                return null
            byteBuffer.position(0)
            return OpenBCI.Packet.fromByteBuf(byteBuffer)

        }

        fun createPacketStreamObservable(): Observable<OpenBCI.Packet> {
            return Observable.create { emitter ->
                try {
                    while (!emitter.isDisposed) {
                        val packet = readPacket()
                        if (packet != null) {
                            emitter.onNext(packet)
                            Thread.sleep(4)
                        } else {
                            break
                        }
                    }
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        }


    }
    companion object {
        fun commonDocumentDirPath(): File {
            /* val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + File.separator +"AlphaTraining")
             Log.i("FileRecorder", "dir is: $dir")
             Log.i("FileRecorder", "succeeded to create a dir: ${dir.mkdir()}")
             return dir*/

            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "AlphaTraining"
            )
            dir.mkdirs()
            return dir
        }

        private fun getBaseName(): String {
            val locale: Locale = Locale.getDefault()
            val formatter = SimpleDateFormat(FILE_NAME_FMT, locale)
            return formatter.format(Calendar.getInstance().time)
        }

        fun getLastRecordedFile(): File {
            val dir: File = commonDocumentDirPath()
            val listOfFiles = dir.listFiles()?.sortedBy { it.name }
            return listOfFiles!!.last()
        }
    }
}