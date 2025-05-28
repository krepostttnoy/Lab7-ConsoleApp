package org.example.commands.consoleCommands

import kotlinx.serialization.Serializable
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseWrapper

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
    fun execute(request: RequestWrapper, username: String): ResponseWrapper
    fun getInfo(): String
    fun getArgsType(): Map<String, String>

}