package org.example.commands.consoleCommands

import org.example.commands.CommandReceiver

class UnknownCommand(
    private val commandReceiver: CommandReceiver,
    private val name: String,
    argTypes1: String,
    private val argTypes: Map<String, String>
): Command {
    override val interactive = false

    override fun execute(args: String?) {
        commandReceiver.unknownCommand(name, argTypes, args)
    }
}