package org.example.commands

import org.example.commands.consoleCommands.Command
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

class CommandInvoker(
    private val outputManager: OutputManager,
    private val inputManager: InputManager
    ) {
    private var commands:Map<String, Command> = mapOf()


    fun clearCommandsMap(){
        commands = mapOf()
    }

    fun register(name: String, command: Command){
        commands += name to command
    }

    fun executeCommand(commandName: String, args: Map<String, String>): ResponseWrapper{
        val command = commands[commandName] ?: return ResponseWrapper(ResponseType.ERROR, "Unknown command: $commandName")
        return try {
            if(command.interactive && inputManager.isScriptMode()) {
                inputManager.switchToInteractive()
                command.execute(args)
                inputManager.returnToScript()
                ResponseWrapper(ResponseType.OK, "Command executed successfully")
            }else{
                command.execute(args)
                ResponseWrapper(ResponseType.OK, "Command executed successfully")
            }
        }catch (e: Exception){
            ResponseWrapper(ResponseType.ERROR, e.message ?: "Execution failed")
        }
    }

    fun executeCommand(commandStr: String) {
        val parts = commandStr.trim().split("\\s+".toRegex())
        val commandName = parts[0].lowercase()
        val args = if (parts.size > 1) parts[1] else null

        val command = commands[commandName]
        if (command == null) {
            outputManager.surePrint("Unknown command. Available commands: ${commands.keys.joinToString(", ")}")
            return
        }

        if(command.interactive && inputManager.isScriptMode()) {
            inputManager.switchToInteractive()
            command.execute(args)
            inputManager.returnToScript()
        }else{
            command.execute(args)
        }
    }

    fun getCommand(): Map<String, Command>{
        return commands
    }
}