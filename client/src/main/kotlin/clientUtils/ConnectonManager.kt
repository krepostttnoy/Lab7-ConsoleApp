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
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.commands.CommandInvoker
import utils.inputOutput.InputManager

class ConnectionManager(private var host: String, private var port: Int) {
    private val timeout = 10000
    private var datagramSocket = DatagramSocket()
    val outputManager = IOThread.outputManager
    val inputManager = InputManager(outputManager)
    private val commandInvoker = CommandInvoker(outputManager, inputManager)
    private var hostInetAddress = InetAddress.getByName(host)
    private var datagramPacket = DatagramPacket(ByteArray(4096), 4096, hostInetAddress, port)
    private val logger: Logger = LogManager.getLogger(ConnectionManager::class.java)

    fun connect(): Boolean{
        datagramSocket.soTimeout = timeout
        return ping() < timeout
    }

    fun ping(): Double {
        val request = RequestWrapper(RequestType.PING, "Ping", mapOf("sender" to host))
        try {
            val start = System.nanoTime()
            send(request)

            datagramSocket.soTimeout = 5000

            receive()
            var ping = (System.nanoTime() - start) / 1_000_000.0
            return ping
            logger.info(ping)
        } catch (e: Exception) {
            logger.error("Ping failed - `{}`: ${e.message}", timeout.toDouble())
            return timeout.toDouble()
        }
    }

    fun send(request: RequestWrapper) {
        val json = Json.encodeToString(RequestWrapper.serializer(), request)
        val buf = json.toByteArray()
        val packet = DatagramPacket(buf, buf.size, InetAddress.getByName(host), port)

        logger.info("Sending to $host:$port: $json")
        datagramSocket.send(packet)
    }

    fun receive(): ResponseWrapper {
        val buf = ByteArray(4096)
        val packet = DatagramPacket(buf, buf.size)
        datagramSocket.receive(packet)
        val json = String(packet.data, 0, packet.length).trim()
        logger.info("Received from ${packet.address}:${packet.port}: $json")
        return Json.decodeFromString(json)
    }

    fun checkSendReceive(request: RequestWrapper): ResponseWrapper{
        try {
            send(request)
        }catch (e: Exception){
            return ResponseWrapper(ResponseType.ERROR, e.message.toString(), receiver = "")
        }
        return receive()
    }
}