package org.example.commands.consoleCommands

import org.example.commands.consoleCommands.ICommandExecutor

/**
 * Команда для вывода списка доступных команд.
 * Использует [ICommandExecutor] для получения и вывода информации о командах.
 *
 * @property ce Исполнитель команд, который предоставляет информацию о доступных командах.
 * @constructor Создаёт команду [HelpCommand] с заданным исполнителем [ce].
 */
class HelpCommand(private val ce: ICommandExecutor) : Command {
    override val interactive = false
    private val argsType = mapOf(
        "" to ""
    )
    private val info = "Returns the list of commands"

    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }

    /**
     * Выполняет команду вывода списка доступных команд.
     * Вызывает метод [ICommandExecutor.getHelp] для отображения списка.
     */
    override fun execute(args: Map<String, String>) {
        TODO("Not yet implemented")
    }
}