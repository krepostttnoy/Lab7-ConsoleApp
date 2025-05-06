package org.example.serverUtils

import baseClasses.ExitFlag.exitFlag
import collection.CollectionManager
import console.ConsoleVehicleManager
import org.example.commands.CommandInvoker
import org.example.commands.consoleCommands.AddCommand
import org.example.commands.consoleCommands.AddIfMaxCommand
import org.example.commands.consoleCommands.AvgOfEnginePowerCommand
import org.example.commands.consoleCommands.ClearCommand
import org.example.commands.consoleCommands.CountGrThanEngPwCommand
import org.example.commands.consoleCommands.InfoCommand
import org.example.commands.consoleCommands.MinByFuelTypeCommand
import org.example.commands.consoleCommands.RemoveAtCommand
import org.example.commands.consoleCommands.RemoveByIdCommand
import org.example.commands.consoleCommands.RemoveGreaterCommand
import org.example.commands.consoleCommands.ShowCommand
import org.example.commands.consoleCommands.UpdateIdCommand
import utils.JsonCreator
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager
import utils.wrappers.RequestType
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.math.log

class Console {
    private val connectionManager = ConnectionManager()
    private val outputManager = OutputManager()
    private val inputManager = InputManager(outputManager)
    private val collectionManager = CollectionManager()
    private val reader = Reader(outputManager, inputManager)
    private val validator = Validator(outputManager, inputManager)
    private val vehicleManager = ConsoleVehicleManager(reader)
    private val fileManager = ConsoleFileManager(collectionManager, outputManager, inputManager)
    private val commandInvoker = CommandInvoker(outputManager, inputManager)
    private val jsonCreator = JsonCreator()
    private val selector = Selector.open()
    private val logger: Logger = LogManager.getLogger(Console::class.java)

    init {
        fileManager.startAutoSave(60)
    }

    fun startServer(host: String, port: Int){
        connectionManager.startServer(host, port)
    }


    fun initialize() {
        logger.info("Initializing console commands")
        commandInvoker.register("add", AddCommand(collectionManager, vehicleManager, outputManager, connectionManager))
        logger.info("Registering Add Command")
        commandInvoker.register("add_if_max", AddIfMaxCommand(collectionManager, vehicleManager, outputManager, connectionManager))
        logger.info("Registering AddIfMax Command")
        commandInvoker.register("avg_of_eng_pw", AvgOfEnginePowerCommand(collectionManager, outputManager, connectionManager))
        logger.info("Registering AvgOfEnginePower Command")
        commandInvoker.register("clear", ClearCommand(collectionManager, outputManager, connectionManager))
        logger.info("Registering Clear Command")
        commandInvoker.register(
            "count_gr_than_eng_pw",
            CountGrThanEngPwCommand(collectionManager, validator, outputManager, connectionManager)
        )
        logger.info("Registering CountGreaterThanEnginePower Command")
        commandInvoker.register("info", InfoCommand(collectionManager, connectionManager))
        logger.info("Registering Info Command")
        commandInvoker.register("min_by_fuel", MinByFuelTypeCommand(collectionManager, outputManager, connectionManager))
        logger.info("Registering MinByFuelType Command")
        commandInvoker.register("remove_at", RemoveAtCommand(collectionManager, validator, outputManager, connectionManager))
        logger.info("Registering RemoveAt Command")
        commandInvoker.register("remove_by_id", RemoveByIdCommand(collectionManager, outputManager, inputManager, connectionManager))
        logger.info("Registering RemoveById Command")
        commandInvoker.register("remove_greater", RemoveGreaterCommand(collectionManager, validator, outputManager, connectionManager))
        logger.info("Registering RemoveGreater Command")
        commandInvoker.register("show", ShowCommand(collectionManager, outputManager, connectionManager))
        logger.info("Registering Show Command")
        commandInvoker.register("update_id", UpdateIdCommand(collectionManager, outputManager, inputManager, connectionManager))
        logger.info("Registering UpdateId Command")
    }

    fun onConnect() {
        val sendingData = mutableMapOf<String, MutableMap<String, String>>(
            "commands" to mutableMapOf(),
            "arguments" to mutableMapOf()
        )
        val commands = commandInvoker.getCommand()

        for (command in commands.keys) {
            sendingData["commands"]!! += (command to commands[command]!!.getInfo())
            sendingData["arguments"]!! += (command to jsonCreator.objectToString(commands[command]!!.getArgsType()))
        }

        val response = ResponseWrapper(ResponseType.SYSTEM, jsonCreator.objectToString(sendingData))
        connectionManager.send(response)
        logger.info("Sending map of commands.")
    }

    fun startInteractiveMode() {
        logger.debug("Starting an interactive mode.")
        connectionManager.datagramChannel.register(selector, SelectionKey.OP_READ)

        try {
            while (!exitFlag) {
                selector.select()
                if(exitFlag) break
                val selectedKeys = selector.selectedKeys()
                val iter = selectedKeys.iterator()
                while (iter.hasNext()) {
                    val key = iter.next()
                    if (key.isReadable) {
                        val client = key.channel() as DatagramChannel
                        try {
                            connectionManager.datagramChannel = client
                            val request = connectionManager.receive()

                            when (request.requestType) {
                                RequestType.COMMAND_EXEC -> {
                                    val commandName = request.message
                                    val args = request.args
                                    commandInvoker.executeCommand(commandName, args)
                                }

                                RequestType.INITIALIZATION -> {
                                    onConnect()
                                }

                                RequestType.PING -> {
                                    val response = ResponseWrapper(ResponseType.SYSTEM, "Pong")
                                    connectionManager.send(response)
                                }
                            }
                        } catch (e: Exception) {
                            val response = ResponseWrapper(ResponseType.ERROR, e.message.toString())
                            logger.error("Error: `{}`", e.message)
                            connectionManager.send(response)
                        }
                    }
                }
            }
        }finally {
            logger.info("Server stopped the work.")
            connectionManager.datagramChannel.close()  // Закрытие канала
            selector.close()
        }
    }

    fun stop(){
        logger.debug("Stop called")
        exitFlag = true
        logger.debug("ExitFlag: `{}`", exitFlag)
        selector.wakeup()
        fileManager.stopAutoSave()
    }

    fun save(){
        try {
            fileManager.saveToFile()
            logger.info("Collection was saved.")
        }catch (e: Exception){}
    }

}