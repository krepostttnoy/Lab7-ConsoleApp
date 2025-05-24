package org.example.commands

import org.example.commands.consoleCommands.Command
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

class CommandInvoker(
    private val inputManager: InputManager
    ) {
    private var commands:Map<String, Command> = mapOf()


    fun clearCommandsMap(){
        commands = mapOf()
    }

    fun register(name: String, command: Command){
        commands += name to command
    }

    fun executeCommand(request: RequestWrapper, username: String): ResponseWrapper{
        val command = commands[request.message] ?: return ResponseWrapper(ResponseType.ERROR, "", receiver = request.args["sender"]!!)
        return try {
            if(command.interactive && inputManager.isScriptMode()) {
                inputManager.switchToInteractive()
                command.execute(request.args, username)
                inputManager.returnToScript()
                ResponseWrapper(ResponseType.OK, "Command executed successfully", receiver = request.args["sender"]!!)
            }else{
                command.execute(request.args, username)
                ResponseWrapper(ResponseType.OK, "Command executed successfully", receiver = request.args["sender"]!!)
            }
        }catch (e: Exception){
            ResponseWrapper(ResponseType.ERROR, e.message ?: "Execution failed", receiver = request.args["sender"]!!)
        }
    }

    fun getCommand(): Map<String, Command>{
        return commands
    }
}