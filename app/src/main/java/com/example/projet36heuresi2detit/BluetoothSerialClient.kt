import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.*

class MyBluetoothClient(private val device: BluetoothDevice, private val context: Context) {

    companion object {
        val UUID_SPP: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private var socket: BluetoothSocket? = null
    private var input: InputStream? = null

    suspend fun connectAndReceiveImage(onImageReceived: (ByteArray) -> Unit) {
        withContext(Dispatchers.IO) {
            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // You can show an error message or throw
                    throw SecurityException("BLUETOOTH_CONNECT permission not granted")
                }

                socket = device.createRfcommSocketToServiceRecord(UUID_SPP)
                socket?.connect()
                input = socket?.inputStream

                // Send command
                socket?.outputStream?.write("CAPTURE\n".toByteArray())

                // Read header line
                val header = buildString {
                    var ch: Int
                    while (input?.read().also { ch = it ?: -1 } != -1 && ch != '\n'.code) {
                        append(ch.toChar())
                    }
                }

                if (!header.startsWith("IMG")) return@withContext
                val length = header.split(" ")[1].toInt()

                // Read image bytes
                val imageBytes = ByteArray(length)
                var bytesRead = 0
                while (bytesRead < length) {
                    val result = input?.read(imageBytes, bytesRead, length - bytesRead) ?: break
                    bytesRead += result
                }

                onImageReceived(imageBytes)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                disconnect()
            }
        }
    }

    fun disconnect() {
        try {
            socket?.close()
            input?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
