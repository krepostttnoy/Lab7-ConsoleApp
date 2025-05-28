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
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.dbConnect.DbManager
import org.example.mutlithread.ReceiverThread
import org.example.mutlithread.SenderThread
import org.example.token.JWTManager
import org.example.users.UserManager
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.LinkedBlockingQueue
import utils.wrappers.Sending
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Console {
    val connectionManager = ConnectionManager()
    private val outputManager = OutputManager()
    private val inputManager = InputManager(outputManager)
    private val collectionManager = CollectionManager()
    private val reader = Reader(outputManager, inputManager)
    private val validator = Validator(outputManager, inputManager)
    private val vehicleManager = ConsoleVehicleManager(reader)
    private val commandInvoker = CommandInvoker(inputManager)
    private val jsonCreator = JsonCreator()
    private val selector = Selector.open()
    private val logger: Logger = LogManager.getLogger(Console::class.java)
    private val dbManager = DbManager("jdbc:postgresql://localhost:15432/studs", "s474305", System.getenv("DB_PASSWORD") ?: throw IllegalStateException("DB_PASSWORD must be set"))
    private val userManager = UserManager(dbManager)
    private val fileManager = ConsoleFileManager(collectionManager, dbManager)
    private val jwtManager = JWTManager()

    //multithread
    private val cachedPool = Executors.newCachedThreadPool()
    private val forkJoinPool = ForkJoinPool.commonPool()
    private val taskQueue = LinkedBlockingQueue<Sending>(10)
    private val answerQueue = LinkedBlockingQueue<Sending>(10)
    private val threadReceiver = ReceiverThread(taskQueue, fileManager, jwtManager, connectionManager, commandInvoker, userManager, jsonCreator, answerQueue)
    private val threadSender = SenderThread(answerQueue, connectionManager)

    init {
        fileManager.startAutoSave(60, userManager)
    }

    fun startServer(host: String, port: Int){
        connectionManager.startServer(host, port)
    }


    fun initialize() {
        dbManager.initDB()
        dbManager.getUsers()
        fileManager.loadCollection()
        logger.info("Initializing console commands")
        commandInvoker.register("add", AddCommand(collectionManager, vehicleManager, outputManager, connectionManager))
        logger.info("Registering Add Command")
        commandInvoker.register("add_if_max", AddIfMaxCommand(collectionManager, vehicleManager, outputManager, connectionManager))
        logger.info("Registering AddIfMax Command")
        commandInvoker.register("avg_of_eng_pw", AvgOfEnginePowerCommand(collectionManager, outputManager, connectionManager))
        logger.info("Registering AvgOfEnginePower Command")
        commandInvoker.register("clear", ClearCommand(collectionManager, outputManager, connectionManager))
        logger.info("Registering Clear Command")
        commandInvoker.register("count_gr_than_eng_pw", CountGrThanEngPwCommand(collectionManager, validator, outputManager, connectionManager))
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

//    fun onConnect() {
//        val sendingData = mutableMapOf<String, MutableMap<String, String>>(
//            "commands" to mutableMapOf(),
//            "arguments" to mutableMapOf()
//        )
//        val commands = commandInvoker.getCommand()
//
//        for (command in commands.keys) {
//            sendingData["commands"]!! += (command to commands[command]!!.getInfo())
//            sendingData["arguments"]!! += (command to jsonCreator.objectToString(commands[command]!!.getArgsType()))
//        }
//
//        val response = ResponseWrapper(ResponseType.SYSTEM, jsonCreator.objectToString(sendingData))
//        connectionManager.send(response)
//        logger.info("Sending map of commands.")
//    }

    fun startInteractiveMode() {
        logger.info("The server is ready to receive commands")
        connectionManager.datagramChannel.register(selector, SelectionKey.OP_READ)

        while (!exitFlag) {
            selector.select()
            val selectedKeys = selector.selectedKeys()

            if (selectedKeys.isEmpty()) continue

            val iter = selectedKeys.iterator()

            while (iter.hasNext()) {
                val key = iter.next()
                iter.remove()
                if (key.isReadable) {
                    val client = key.channel() as DatagramChannel
                    connectionManager.datagramChannel = client

                    val query = connectionManager.receive()
                    taskQueue.put(query)

                    cachedPool.execute {
                        threadReceiver.run()
                    }

                    forkJoinPool.execute {
                        threadSender.run()
                    }

                }
            }
        }
    }

    fun stop(){
        logger.debug("Stop called")
        exitFlag = true
        logger.debug("ExitFlag: `{}`", exitFlag)
        cachedPool.awaitTermination(5000, TimeUnit.MILLISECONDS)
        forkJoinPool.awaitTermination(5000, TimeUnit.MILLISECONDS)
        connectionManager.datagramChannel.close()
        selector.wakeup()
        fileManager.stopAutoSave()
    }

    fun save(){
        try {
            fileManager.saveCollection(userManager)
            logger.info("Collection was saved.")
        }catch (e: Exception){
            logger.warn("Collection was not saved: ${e.message}")
        }
    }

}