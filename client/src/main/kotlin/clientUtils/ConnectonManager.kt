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
import utils.exceptions.CircuitBreakerOpenException
import utils.inputOutput.InputManager
import java.io.IOException

class ConnectionManager(private var host: String, private var port: Int) {
    private val timeout = 10000
    private var datagramSocket = DatagramSocket()
    val outputManager = IOThread.outputManager
    val inputManager = InputManager(outputManager)
    private val commandInvoker = CommandInvoker(outputManager, inputManager)
    private var hostInetAddress = InetAddress.getByName(host)
    private var datagramPacket = DatagramPacket(ByteArray(4096), 4096, hostInetAddress, port)
    private val logger: Logger = LogManager.getLogger(ConnectionManager::class.java)
    private val circuitBreaker = CircuitBreaker()

    fun connect(): Boolean{
        datagramSocket.soTimeout = timeout
        return ping() < timeout
    }

    fun ping(): Double {
        val request = RequestWrapper(RequestType.PING, "Ping", mutableMapOf("sender" to host))
        try {
            val start = System.nanoTime()
            send(request)

            datagramSocket.soTimeout = 7000

            receive()
            var ping = (System.nanoTime() - start) / 1_000_000.0
            logger.info(ping)
            return ping
        } catch (e: Exception) {
            logger.error("Ping failed - `{}`: ${e.message}", timeout.toDouble())
            return timeout.toDouble()
        }
    }

    fun send(request: RequestWrapper) {
        if(circuitBreaker.allowRequest()){
            try {
                val json = Json.encodeToString(RequestWrapper.serializer(), request)
                val buf = json.toByteArray()
                val packet = DatagramPacket(buf, buf.size, InetAddress.getByName(host), port)

                logger.info("Sending to $host:$port: $json")
                datagramSocket.send(packet)
                circuitBreaker.recordSuccess()
            }catch (e: Exception){
                circuitBreaker.recordFailure()
                logger.error("Failed to send request: ${e.message}")
            }
        }else {
            throw CircuitBreakerOpenException("Circuit breaker is open. Requests are blocked.")
        }

    }

    fun receive(): ResponseWrapper {
        try {
            val buf = ByteArray(4096)
            val packet = DatagramPacket(buf, buf.size)
            datagramSocket.receive(packet)
            val json = String(packet.data, 0, packet.length).trim()
            if (json.isEmpty()) {
                throw IOException("Empty response from server")
            }
            logger.info("Received from ${packet.address}:${packet.port}: $json")
            circuitBreaker.recordSuccess()
            return Json.decodeFromString(json)
        } catch (e: Exception) {
            circuitBreaker.recordFailure()
            logger.error("Failed to receive response: ${e.message}")
            throw e
        }
    }

    fun checkSendReceive(request: RequestWrapper): ResponseWrapper{
        datagramSocket.soTimeout = 20000
        try {
            send(request)
            return receive()
        } catch (e: Exception) {
            return ResponseWrapper(ResponseType.ERROR, e.message.toString(), receiver = "")
        }
    }
}