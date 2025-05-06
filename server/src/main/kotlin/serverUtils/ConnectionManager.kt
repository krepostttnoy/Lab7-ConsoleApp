package org.example.serverUtils

import utils.IOThread
import utils.JsonCreator
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseWrapper
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class ConnectionManager() {
    private var host = "localhost"
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
}