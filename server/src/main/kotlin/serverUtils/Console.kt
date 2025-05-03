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
import kotlin.system.exitProcess

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

    init {
        fileManager.startAutoSave(60)
    }

    fun startServer(host: String, port: Int){
        connectionManager.startServer(host, port)
    }


    fun initialize() {
        commandInvoker.register("add", AddCommand(collectionManager, vehicleManager, outputManager, connectionManager))
        commandInvoker.register("add_if_max", AddIfMaxCommand(collectionManager, vehicleManager, outputManager, connectionManager))
        commandInvoker.register("avg_of_eng_pw", AvgOfEnginePowerCommand(collectionManager, outputManager, connectionManager))
        commandInvoker.register("clear", ClearCommand(collectionManager, outputManager, connectionManager))
        commandInvoker.register(
            "count_gr_than_eng_pw",
            CountGrThanEngPwCommand(collectionManager, validator, outputManager, connectionManager)
        )
        commandInvoker.register("info", InfoCommand(collectionManager, connectionManager))
        commandInvoker.register("min_by_fuel", MinByFuelTypeCommand(collectionManager, outputManager, connectionManager))
        commandInvoker.register("remove_at", RemoveAtCommand(collectionManager, validator, outputManager, connectionManager))
        commandInvoker.register("remove_by_id", RemoveByIdCommand(collectionManager, outputManager, inputManager, connectionManager))
        commandInvoker.register("remove_greater", RemoveGreaterCommand(collectionManager, validator, outputManager, connectionManager))
        commandInvoker.register("show", ShowCommand(collectionManager, outputManager, connectionManager))
        commandInvoker.register("update_id", UpdateIdCommand(collectionManager, outputManager, inputManager, connectionManager))
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
    }

    fun startInteractiveMode() {
        connectionManager.datagramChannel.register(selector, SelectionKey.OP_READ)

        try {
            while (!exitFlag) {
                selector.select()

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
                                    val response = ResponseWrapper(ResponseType.OK,
                                        commandInvoker.executeCommand(commandName, args).toString()
                                    )

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
                            connectionManager.send(response)
                        }
                    }
                }
            }
        }finally {
            outputManager.println("Server stopped the work.")
            connectionManager.datagramChannel.close()  // Закрытие канала
            selector.close()
        }
    }

    fun stop(){
        println("DEBUG: Stop called")
        exitFlag = true
        selector.wakeup()
    }

    fun save(){
        try {
            fileManager.saveToFile()
            outputManager.println("Collection was saved.")
        }catch (e: Exception){}
    }

}