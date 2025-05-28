package org.example.commands

import org.example.commands.consoleCommands.Command
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager

class CommandInvoker(
    private val outputManager: OutputManager,
    private val inputManager: InputManager
    ) {
    private var commands:Map<String, Command> = mapOf()


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

        command.execute(args)
    }

    fun getCommand(): Map<String, Command>{
        return commands
    }
}