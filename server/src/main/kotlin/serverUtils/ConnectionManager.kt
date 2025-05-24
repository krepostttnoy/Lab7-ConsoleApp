package org.example.serverUtils

import kotlinx.serialization.json.Json
import utils.IOThread
import utils.JsonCreator
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseWrapper
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.wrappers.ResponseType

class ConnectionManager() {
    private var host = "0.0.0.0"
    private var port = 6789
    private var address = InetSocketAddress(host, port)
    var datagramChannel = DatagramChannel.open()
    private var buffer = ByteBuffer.allocate(4096)
    private var remoteAddress = InetSocketAddress(port)
    private val jsonCreator = JsonCreator()
    private val outputManager = IOThread.outputManager
    private val logger: Logger = LogManager.getLogger(ConnectionManager::class.java)

    fun startServer(host: String, port: Int){
        this.host = host
        this.port = port
        datagramChannel = DatagramChannel.open().apply {
            bind(InetSocketAddress(host, port))
            configureBlocking(false)
        }
        logger.info("Server bound to: ${datagramChannel.localAddress}")
    }

    fun receive(): RequestWrapper{
        buffer.clear()
        remoteAddress = (datagramChannel.receive(buffer)) as InetSocketAddress
        buffer.flip()
        val json = String(buffer.array(), 0, buffer.limit()).trim()
        return jsonCreator.stringToObject(json)
        logger.info("Received from $remoteAddress:\n$json")
    }

    fun send(response: ResponseWrapper){
        val json = jsonCreator.objectToString(response)
        logger.info("Sending to $remoteAddress:\n$json")
        buffer.clear()
        buffer.put(json.toByteArray())
        buffer.flip()
        datagramChannel.send(buffer, remoteAddress)
    }

    fun registrationRequest(host: String, port: Int, message: String) {
        val request = ResponseWrapper(ResponseType.REG_REQUEST, message, receiver = "")
        buffer = ByteBuffer.allocate(8192)
        val jsonAnswer = Json.encodeToString(ResponseWrapper.serializer(), request).toByteArray()
        val data = ByteBuffer.wrap(jsonAnswer)
        val receiver = InetSocketAddress(host, port)
        datagramChannel.send(data, receiver)
    }
}