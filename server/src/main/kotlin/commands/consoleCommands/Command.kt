package org.example.commands.consoleCommands

import kotlinx.serialization.Serializable

/**
 * Интерфейс для реализации команд.
 * Определяет контракт для всех команд, которые должны иметь метод execute.
 */
@Serializable
sealed interface Command {

    /**
     * Выполняет команду.
     * Реализация метода зависит от конкретной команды.
     */
    val interactive: Boolean
    fun execute(args: String?)
    fun execute(args: Map<String, String>)
    fun getInfo(): String
    fun getArgsType(): Map<String, String>

}