package org.example.mutlithread

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.serverUtils.ConnectionManager
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper
import utils.wrappers.Sending
import java.util.concurrent.LinkedBlockingQueue

class SenderThread(private val answerQueue: LinkedBlockingQueue<Sending>,
                   private val connectionManager: ConnectionManager
) : Runnable {
    var answer = ResponseWrapper(ResponseType.ERROR, "Unknown error", receiver = "", token = "")
    private val logger: Logger = LogManager.getLogger(SenderThread::class.java)
    override fun run() {
        answer = answerQueue.take() as ResponseWrapper
        logger.debug("Sending: {}", answer.message)
        connectionManager.send(answer)
    }

}