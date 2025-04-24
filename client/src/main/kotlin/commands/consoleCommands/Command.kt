package org.example.commands.consoleCommands

/**
 * Интерфейс для реализации команд.
 * Определяет контракт для всех команд, которые должны иметь метод execute.
 */
interface Command {

    /**
     * Выполняет команду.
     * Реализация метода зависит от конкретной команды.
     */
    val interactive: Boolean
    fun execute(args: String?)

}