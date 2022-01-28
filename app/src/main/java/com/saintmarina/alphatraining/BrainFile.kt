package com.saintmarina.alphatraining

import android.os.Build
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*


private const val FILE_NAME_FMT: String = "yyyy-MM-d HH.mm.ss"
//val destination = File("/sdcard")

class BrainFile {
    private var outputStream: FileOutputStream = FileOutputStream(File(commonDocumentDirPath("AlphaTraining"), getBaseName()))
    private var byteBuffer: ByteBuffer = ByteBuffer.allocate(36)

    fun write(packet: OpenBCI.Packet) {
        byteBuffer.position(0)
        packet.fillByteBuf(byteBuffer)
        outputStream.channel.write(byteBuffer)
    }

    fun close() {
        outputStream.flush()
        outputStream.close()
    }

    private fun commonDocumentDirPath(folderName: String): File? {
        var dir: File? = null
        dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + "/" + folderName
            )
        } else {
            File(Environment.getExternalStorageDirectory().toString() + "/" + folderName)
        }

        // Make sure the path directory exists.
        if (!dir.exists()) {
            // Make it, if it doesn't exit
            val success = dir.mkdirs()
            if (!success) {
                dir = null
            }
        }
        return dir
    }

    private fun getBaseName(): String {
        val locale: Locale = Locale.getDefault()
        val formatter = SimpleDateFormat(FILE_NAME_FMT, locale)
        return formatter.format(Calendar.getInstance().time)
    }
}