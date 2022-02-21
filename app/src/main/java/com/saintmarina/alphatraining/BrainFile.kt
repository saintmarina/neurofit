package com.saintmarina.alphatraining

import android.content.Context
import android.os.Environment
import android.util.Log
import io.reactivex.rxjava3.core.Observable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

private const val FILE_NAME_FMT: String = "yyyy-MM-d HH.mm.ss"

class BrainFile {
    private var byteBuffer: ByteBuffer = ByteBuffer.allocate(36)
    inner class Writer(path: File) {
        private var outputStream: FileOutputStream = FileOutputStream(path)

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

    inner class Reader(path: File) {
        private val inputStream = FileInputStream(path)

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
        fun commonDocumentDirPath(context: Context): File {
             val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + File.separator +"AlphaTraining")
             Log.i("FileRecorder", "dir is: $dir")
             Log.i("FileRecorder", "succeeded to create a dir: ${dir.mkdir()}")
             return dir

            /*
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "AlphaTraining"
            )
            dir.mkdirs()
            return dir*/
        }

         fun getDefaultFileName(): String {
            val locale: Locale = Locale.getDefault()
            val formatter = SimpleDateFormat(FILE_NAME_FMT, locale)
            return formatter.format(Calendar.getInstance().time)
        }


         fun getLastRecordedFile(context: Context): File {
            val dir: File = commonDocumentDirPath(context)
            val listOfFiles = dir.listFiles()?.sortedBy { it.name }
            Log.i("X", "Listing files in ${dir}: $listOfFiles")
            return listOfFiles!!.last()
        }

        fun getNumberOfFilesInInternalStorage(context: Context): Int {
            val dir: File = commonDocumentDirPath(context)
            return dir.listFiles()?.size ?: 0
        }

        fun loadDemoBrainDataToDisk(context: Context) {
            val outputStream = FileOutputStream(File(commonDocumentDirPath(context), getDefaultFileName()))
            val inputStream = context.resources.openRawResource(R.raw.brain_data)
            val buffer = ByteArray(4*1024)

            var read = inputStream.read(buffer)
            while (read != -1) {
                outputStream.write(buffer, 0, read)
                read = inputStream.read(buffer)

            }
            outputStream.flush()
            inputStream.close()
        }
    }

}