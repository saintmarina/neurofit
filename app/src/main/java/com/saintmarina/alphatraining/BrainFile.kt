package com.saintmarina.alphatraining

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*


private const val FILE_NAME_FMT: String = "yyyy-MM-d HH.mm.ss"

class BrainFile(private val context: Context) {
    private var outputStream: FileOutputStream = FileOutputStream(File(commonDocumentDirPath(), getBaseName()))
    private var byteBuffer: ByteBuffer = ByteBuffer.allocate(36)

    fun write(packet: OpenBCI.Packet) {
        byteBuffer.position(0)
        packet.fillByteBuf(byteBuffer)
        byteBuffer.position(0)
        outputStream.channel.write(byteBuffer)
    }

    fun close() {
        outputStream.flush()
        outputStream.close()
    }

    private fun commonDocumentDirPath(): File? {
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
}