package org.example.commands.consoleCommands

import baseClasses.ExitFlag
import utils.inputOutput.OutputManager
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseWrapper

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
    private val argsType = mapOf(
        "" to ""
    )
    private val info = "Finishes the work"

    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }

    /**
     * Выполняет команду завершения программы.
     * Выводит сообщение "Программа завершена." и завершает процесс с кодом 0.
     */
    override fun execute(request: RequestWrapper, username: String): ResponseWrapper {
        TODO("Not yet implemented")
    }
}