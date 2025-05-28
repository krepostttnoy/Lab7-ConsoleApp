package org.example.commands

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.commands.consoleCommands.Command
import org.example.commands.consoleCommands.CommandTest
import org.example.mutlithread.ReceiverThread
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

class CommandInvoker(
    private val inputManager: InputManager
    ) {
    private var commands:Map<String, Command> = mapOf()
    private val logger: Logger = LogManager.getLogger(CommandInvoker::class.java)


    fun clearCommandsMap(){
        commands = mapOf()
    }

    fun register(name: String, command: Command){
        commands += name to command
    }

    fun executeCommand(request: RequestWrapper, username: String): ResponseWrapper{
        logger.info("execute command")
        val command = commands[request.message]!!// return ResponseWrapper(ResponseType.ERROR, "", receiver = request.args["sender"]!!)
        logger.info("command val create")
        return try {
            if(command.interactive && inputManager.isScriptMode()) {
                inputManager.switchToInteractive()
                val result = command.execute(request, username)
                inputManager.returnToScript()
                result
                //ResponseWrapper(ResponseType.OK, "Command executed successfully", receiver = request.args["sender"]!!)
            }else{
                logger.info("command execute tries")
                val result = command.execute(request, username)
                logger.info("zalupa")
                result
                //ResponseWrapper(ResponseType.OK, "Command executed successfully", receiver = username)
            }
        }catch (e: Exception){
            ResponseWrapper(ResponseType.OK, e.message ?: "Execution failed", receiver = username)
        }
    }

    fun getCommand(): Map<String, Command>{
        return commands
    }
}