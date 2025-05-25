package org.example.mutlithread

import collection.CollectionManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.commands.CommandInvoker
import org.example.serverUtils.ConnectionManager
import org.example.serverUtils.ConsoleFileManager
import org.example.token.JWTManager
import org.example.users.UserManager
import utils.JsonCreator
import utils.wrappers.RequestType
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper
import utils.wrappers.Sending
import java.util.concurrent.LinkedBlockingQueue

class ReceiverThread(
    private val taskQueue: LinkedBlockingQueue<Sending>,
    private val fileManager: ConsoleFileManager,
    private val jwtManager: JWTManager,
    private val connectionManager: ConnectionManager,
    private val commandInvoker: CommandInvoker,
    private val userManager: UserManager,
    private val jsonCreator: JsonCreator,
    private val answerQueue: LinkedBlockingQueue<Sending>
) : Runnable{
    var answer = ResponseWrapper(ResponseType.ERROR, "Unknown error", receiver = "", token = "")
    private val logger: Logger = LogManager.getLogger(ReceiverThread::class.java)

    override fun run() {
        val query = taskQueue.take() as RequestWrapper
        val receiver = query.args["sender"] ?: "unknown"
        try {
            when (query.requestType) {
                RequestType.COMMAND_EXEC -> {
                    logger.info("Received command: ${query.message}")
                    fileManager.loadCollection()

                    if (jwtManager.validateJWS(query.token)) {
                        val username = jwtManager.retrieveUsername(query.token)
                        answer = commandInvoker.executeCommand(query, username)
                        answer.token = jwtManager.createJWS("server", username)
                    } else {
                        answer = ResponseWrapper(ResponseType.AUTH_ERROR, "Unknown token. Authorize again.", receiver = receiver)
                    }
                }

                RequestType.INITIALIZATION -> {
                    logger.trace("Received initialization request")

                    val sendingInfo = mutableMapOf<String, MutableMap<String, String>>(
                        "commands" to mutableMapOf(),
                        "arguments" to mutableMapOf()
                    )
                    val commands = commandInvoker.getCommand()

                    for (command in commands.keys) {
                        sendingInfo["commands"]!! += (command to commands[command]!!.getInfo())
                        sendingInfo["arguments"]!! += (command to jsonCreator.objectToString(commands[command]!!))
                    }

                    answer = ResponseWrapper(ResponseType.SYSTEM, jsonCreator.objectToString(sendingInfo), receiver = receiver)
                }

                RequestType.PING -> {
                    logger.info("Received ping request from: {}", receiver)
                    answer = ResponseWrapper(ResponseType.SYSTEM, "Pong", receiver = receiver)
                }

                RequestType.AUTHORIZATION -> {
                    logger.info("Received authorization request")
                    if (query.message != "logout") {
                        answer = if (userManager.userExists(query.args["username"]!!)) {
                            val token = userManager.login(query.args["username"]!!, query.args["password"]!!)
                            if (token.isNotEmpty()) {
                                ResponseWrapper(ResponseType.OK, "Authorized", token, receiver = receiver)
                            } else {
                                ResponseWrapper(ResponseType.AUTH_ERROR, "Wrong password", receiver = receiver)
                            }
                        } else {
                            val token = userManager.register(query.args["username"]!!, query.args["password"]!!)
                            if (token.isNotEmpty()) {
                                ResponseWrapper(ResponseType.OK, "Registered", token, receiver = receiver)
                            } else {
                                ResponseWrapper(ResponseType.AUTH_ERROR, "Could not register", receiver = receiver)
                            }
                        }
                    }
                }
                else -> {
                    ""
                }
            }
        } catch (e: Exception) {
            logger.error("Error while executing command: ${e.message}")
            answer = ResponseWrapper(ResponseType.ERROR, e.message.toString(), receiver = receiver)
        } finally {
            answerQueue.put(answer)
        }
    }

}