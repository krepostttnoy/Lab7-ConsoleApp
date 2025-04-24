package org.example.commands.consoleCommands

/**
 * Интерфейс для выполнения команд и предоставления справки.
 * Определяет контракт для классов, которые должны уметь выполнять команды
 * и предоставлять информацию о доступных командах.
 */
interface ICommandExecutor {

    /**
     * Выполняет команду, переданную в виде строки.
     *
     * @param commandStr Строка, содержащая название команды и, возможно, аргументы.
     */
    fun executeCommand(commandStr: String)

    fun getHelp()
}