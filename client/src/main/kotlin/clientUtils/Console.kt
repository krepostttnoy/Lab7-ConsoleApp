import baseClasses.ExitFlag.exitFlag
import commands.ExecuteScriptCommand
import commands.ICommandExecutor
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
import org.example.clientUtils.CircuitBreaker
import org.example.clientUtils.readers.Validator
import java.io.StringReader

class Console{
    private val circuitBreaker = CircuitBreaker()
    private val connectionManager = ConnectionManager("127.0.0.1", 6789, circuitBreaker)
    private val outputManager = IOThread.outputManager
    private val inputManager = IOThread.inputManager
    private val commandInvoker = CommandInvoker(outputManager, inputManager)
    private val commandReceiver = CommandReceiver(outputManager, inputManager, commandInvoker, connectionManager, this, circuitBreaker)
    private val jsonCreator = JsonCreator()
    private val logger: Logger = LogManager.getLogger(Console::class.java)
    private val validator: Validator by lazy {Validator(outputManager, inputManager)}

    private var token = ""
    var authorized: Boolean = false

    fun getConnection(){
        logger.info("Trying to connect...")
        val connected = connectionManager.connect()

        try {
            if(connected){
                authorize()
                initialize()
                registerBasicCommands()
                logger.info("Connected successfully")
                sendAllRequests()
                startInteractiveMode()
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
                    startInteractiveMode()
                }
            }
        }catch (e: Exception){}

    }

    fun getToken(): String{
        return token
    }

    fun authorize(){
        outputManager.surePrint("Sign up/log in for using the collection.")
        val username = getValidUsername()
        val password = getValidPassword()

        val req = RequestWrapper(RequestType.AUTHORIZATION, "", mutableMapOf("username" to username, "password" to password))

        logger.info("Sent request for authorization")
        val response = connectionManager.checkSendReceive(req)
        logger.info("Response received: $response")
        if (response.responseType == ResponseType.AUTH_ERROR || response.responseType == ResponseType.ERROR) {
            outputManager.println(response.message)
            authorize()
        } else {
            logger.info("Authorized")
            token = response.token
            outputManager.surePrint("Goida bratan, "+ username)
            authorized = true
        }
    }

    private fun getValidUsername(): String {
        while (true) {
            outputManager.print("Username: ")
            val username = validator.readLineTrimmed()

            when {
                username.isEmpty() -> {
                    outputManager.println("Username cannot be empty")
                }
                username.length == 1 -> {
                    outputManager.println("Username must be at least 2 characters long")
                }
                else -> return username
            }
        }
    }

    private fun getValidPassword(): String {
        while (true) {
            outputManager.print("Password: ")
            val password = validator.readLineTrimmed()

            when {
                password.isEmpty() -> {
                    outputManager.println("Password cannot be empty")
                }
                password.length == 1 -> {
                    outputManager.println("Password must be at least 2 characters long")
                }
                else -> return password
            }
        }
    }

    fun initialize(){
        logger.info("Initializing console commands")
        val request = RequestWrapper(RequestType.INITIALIZATION, "", mutableMapOf())
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

    //Автор метода - гений. Все отправленные сообщения помещаются в буфер, сохраняется последний элемент и отправляется.
    fun sendAllRequests(){
        val buf = commandReceiver.buffer
        if (buf.isNotEmpty()) {
            val lastElement = buf.last()
            buf.clear()

            println("Sending last request.")
            val response = connectionManager.checkSendReceive(lastElement)
            outputManager.println(response.message)
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
            logger.info("Starting an interactive mode")
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
            getConnection()
        }
    }
}