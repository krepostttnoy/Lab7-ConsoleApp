import baseClasses.ExitFlag.exitFlag
import commands.ExecuteScriptCommand
import org.example.clientUtils.ConnectionManager
import org.example.commands.CommandInvoker
import org.example.commands.CommandReceiver
import org.example.commands.consoleCommands.ExitCommand
import org.example.commands.consoleCommands.HelpCommand
import org.example.commands.consoleCommands.UnknownCommand
import utils.IOThread
import utils.JsonCreator
import utils.wrappers.RequestType
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Console{
    private val connectionManager = ConnectionManager("localhost", 6789)
    private val outputManager = IOThread.outputManager
    private val inputManager = IOThread.inputManager
    private val commandInvoker = CommandInvoker(outputManager, inputManager)
    private val commandReceiver = CommandReceiver(outputManager, inputManager, commandInvoker, connectionManager)
    private val jsonCreator = JsonCreator()
    private val logger: Logger = LogManager.getLogger(Console::class.java)

    fun getConnection(){
        logger.info("Trying to connect...")
        val connected = connectionManager.connect()
        if(connected){
            initialize()
            registerBasicCommands()
            logger.info("Connected successfully")
        }else{
            outputManager.println("No server connection.")
            outputManager.println("Retry connection? [y/n]")
            outputManager.print("$ ")
            var input = inputManager.read().trim().lowercase().split(" ")
            while ((input[0] != "y") and (input[0] != "n")){
                outputManager.println("Wrong input. Try again [y/n]")
                outputManager.print("$ ")
                input = inputManager.read().trim().lowercase().split(" ")
            }
            if (input[0] == "y"){
                getConnection()
            }else{
                registerBasicCommands()
            }
        }

    }

    fun initialize(){
        logger.info("Initializing console commands")
        val request = RequestWrapper(RequestType.INITIALIZATION, "", mapOf())
        val response = connectionManager.checkSendReceive(request)

        if(response.responseType == ResponseType.ERROR){
            outputManager.println(response.message)
        }else{
            val serverCommands = jsonCreator.stringToObject<Map<String, Map<String, String>>>(response.message)
            commandInvoker.clearCommandMap()

            for (i in serverCommands["commands"]!!.keys) {
                commandInvoker.register(
                    i,
                    UnknownCommand(
                        commandReceiver,
                        i,
                        serverCommands["commands"]!![i]!!,
                        jsonCreator.stringToObject(serverCommands["arguments"]!![i]!!)
                    )
                )
            }
        }
    }

    fun registerBasicCommands(){
        logger.info("Registering basic commands")
        commandInvoker.register("help", HelpCommand(commandReceiver))
        commandInvoker.register("exit", ExitCommand(outputManager))
        commandInvoker.register("execute_script", ExecuteScriptCommand(commandInvoker, outputManager, inputManager))
    }

    fun startInteractiveMode(){
        try {
            logger.info("Starting interactive mode")
            while (!exitFlag) {
                outputManager.print("$ ")
                val input = readLine()?.trim()
                if(input == null){
                    return
                }
                if(input.isBlank()) continue
                commandInvoker.executeCommand(input.lowercase())
            }
        } catch (e: IllegalArgumentException) {
            logger.error("${e.message}")
        } catch (e: StackOverflowError) {
            logger.error("${e.message}")
        } catch (e: Exception){
            logger.error("Unknown error: ${e.message}")
        }
    }
}