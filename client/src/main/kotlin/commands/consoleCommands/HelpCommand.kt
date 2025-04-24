package org.example.commands.consoleCommands

import commands.ICommandExecutor
import org.example.commands.CommandReceiver

/**
 * Команда для вывода списка доступных команд.
 * Использует [commands.ICommandExecutor] для получения и вывода информации о командах.
 *
 * @property ce Исполнитель команд, который предоставляет информацию о доступных командах.
 * @constructor Создаёт команду [HelpCommand] с заданным исполнителем [ce].
 */
class HelpCommand(private val commandReceiver: CommandReceiver) : Command {
    override val interactive = false

    /**
     * Выполняет команду вывода списка доступных команд.
     * Вызывает метод [ICommandExecutor.getHelp] для отображения списка.
     */
    override fun execute(args: String?) {
        commandReceiver.getHelp()
    }
}