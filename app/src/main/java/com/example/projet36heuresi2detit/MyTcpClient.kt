package com.example.projet36heuresi2detit

import android.graphics.BitmapFactory
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException

class MyTcpClient(private val serverIp: String, private val serverPort: Int, private val onConnectionFailed: () -> Unit) {
    private var socket: Socket? = null
    private var dataInputStream: DataInputStream? = null

    fun connect() {
        try {
            socket = Socket()
            socket?.connect(InetSocketAddress(serverIp, serverPort), 5000)
            dataInputStream = DataInputStream(BufferedInputStream(socket?.getInputStream()))
        } catch (e: IOException) {
            e.printStackTrace()
            onConnectionFailed()
        }
    }
    fun receiveImage(onImageReceived: (ByteArray) -> Unit) {
        Thread {
            try {
                while (socket?.isConnected == true && socket?.isClosed == false) {
                    val length = dataInputStream?.readInt()
                    if (length != null && length > 0) {
                        val imageBytes = ByteArray(length)
                        dataInputStream?.readFully(imageBytes)
                        onImageReceived(imageBytes)
                    }
                }
            } catch (e: SocketException){
                println("Socket error, disconnected")
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    fun disconnect() {
        try {
            socket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}