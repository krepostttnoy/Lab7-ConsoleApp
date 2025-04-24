package org.example.clientUtils

import kotlinx.serialization.json.Json
import utils.IOThread
import utils.wrappers.RequestType
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class ConnectionManager(private var host: String, private var port: Int) {
    private val timeout = 5000
    private var datagramSocket = DatagramSocket()
    val outputManager = IOThread.outputManager
    private var hostInetAddress = InetAddress.getByName(host)
    private var datagramPacket = DatagramPacket(ByteArray(4096), 4096, hostInetAddress, port)



    fun connect(): Boolean{
        datagramSocket.soTimeout = timeout
        return ping() < timeout
    }

    fun ping(): Double {
        val request = RequestWrapper(RequestType.PING, "Ping", emptyMap())
        try {
            val start = System.nanoTime()
            send(request)

            datagramSocket.soTimeout = 5000

            receive()
            return (System.nanoTime() - start) / 1_000_000.0
        } catch (e: Exception) {
            println("❌ Ping failed: ${e.message}")
            return timeout.toDouble()
        }
    }

    fun send(request: RequestWrapper) {
        val json = Json.encodeToString(RequestWrapper.serializer(), request)
        val buf = json.toByteArray()
        val packet = DatagramPacket(buf, buf.size, InetAddress.getByName(host), port)
        outputManager.println("🚀 Sending to $host:$port: $json")
        datagramSocket.send(packet)
    }

    fun receive(): ResponseWrapper {
        val buf = ByteArray(4096)
        val packet = DatagramPacket(buf, buf.size)
        datagramSocket.receive(packet)
        val json = String(packet.data, 0, packet.length).trim()
        outputManager.println("🔽 Received from ${packet.address}:${packet.port}: $json")
        return Json.decodeFromString(json)
    }

    fun checkSendReceive(request: RequestWrapper): ResponseWrapper{
        try {
            send(request)
        }catch (e: Exception){
            return ResponseWrapper(ResponseType.ERROR, e.message.toString())
        }
        return receive()
    }

    fun handleResponse(responseWrapper: ResponseWrapper){
        when(responseWrapper.message) {
            "Output disabled" -> outputManager.disableOutput()
            "Output enabled" -> outputManager.enableOutput()
        }
    }
}