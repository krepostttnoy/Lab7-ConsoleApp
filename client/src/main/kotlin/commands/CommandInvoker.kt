package org.example.commands

import org.example.commands.consoleCommands.Command
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class CommandInvoker(
    private val outputManager: OutputManager,
    private val inputManager: InputManager
    ) {
    private var commands:Map<String, Command> = mapOf()
    private val logger: Logger = LogManager.getLogger(CommandInvoker::class.java)


    fun clearCommandMap(){
        commands = mapOf()
    }

    fun register(name: String, command: Command){
        commands += name to command
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