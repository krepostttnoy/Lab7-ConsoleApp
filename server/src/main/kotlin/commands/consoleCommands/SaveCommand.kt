package org.example.commands.consoleCommands

import org.example.serverUtils.IFileManager

/**
 * Команда для сохранения коллекции в файл.
 * Использует [org.example.serverUtils.IFileManager] для выполнения операции сохранения.
 *
 * @property fm Менеджер файлов, используемый для сохранения данных.
 * @constructor Создаёт команду [SaveCommand] с заданным менеджером [fm].
 */
class SaveCommand(private val fm: IFileManager) : Command {
    override val interactive = false
    private val argsType = mapOf(
        "" to ""
    )
    private val info = "Saves the collection in file"

    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }

    /**
     * Выполняет команду сохранения коллекции в файл.
     * Если путь к файлу не указан, использует путь по умолчанию, предоставленный [IFileManager].
     *
     * @param filePath Путь к файлу, в который нужно сохранить коллекцию (может быть null).
     */

    /**
     * Выполняет команду без аргументов.
     * Вызывает [execute] с параметром [filePath] равным null,
     * что приводит к использованию пути по умолчанию.
     */
    override fun execute(args: Map<String, String>) {
        TODO("Not yet implemented")
    }

     fun execute(args: String?) {
        fm.saveToFile()
    }
}