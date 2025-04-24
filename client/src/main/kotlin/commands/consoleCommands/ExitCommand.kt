package org.example.commands.consoleCommands

import baseClasses.ExitFlag
import utils.inputOutput.OutputManager

/**
 * Команда для завершения работы программы.
 * Выводит сообщение о завершении и завершает процесс с кодом 0.
 *
 * @property cm Менеджер коллекции, переданный для совместимости с другими командами (не используется).
 * @constructor Создаёт команду [ExitCommand] с заданным менеджером [cm].
 */
class ExitCommand(
    private val outputManager: OutputManager,
    ) : Command {
    override val interactive = false

    /**
     * Выполняет команду завершения программы.
     * Выводит сообщение "Программа завершена." и завершает процесс с кодом 0.
     */
    override fun execute(args: String?) {
        outputManager.disableOutput()
        outputManager.surePrint("Программа завершена.")
        ExitFlag.exitFlag = true
    }
}