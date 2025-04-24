package org.example.commands.consoleCommands

import collection.CollectionManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.commands.CommandInvoker
import org.example.commands.consoleCommands.ICommandExecutor
import org.example.serverUtils.ConnectionManager
import org.example.serverUtils.IFileManager
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager
import java.io.File
import java.lang.Exception

/**
 * Команда для выполнения скрипта из файла.
 * Читает команды из указанного файла построчно и выполняет их с помощью [ICommandExecutor].
 * Использует [collection.CollectionManager] и [org.example.serverUtils.IFileManager] для работы с коллекцией и файлами.
 *
 * @property cm Менеджер коллекции, используемый для выполнения команд.
 * @property fm Менеджер файлов, используемый для операций с файлами.
 * @property ce Исполнитель команд, который выполняет команды из скрипта.
 * @constructor Создаёт команду [ExecuteScriptCommand] с заданными менеджерами [cm], [fm] и [ce].
 */
@Serializable
class ExecuteScriptCommand(
    @Transient private val commandInvoker: CommandInvoker = null!!,
    @Transient private val outputManager: OutputManager = null!!,
    @Transient private val inputManager: InputManager = null!!,
    @Transient private val connectionManager: ConnectionManager = null!!
) : Command {
    override val interactive = true
    private val argsType = mapOf(
        "filePath" to "String"
    )
    private val info = "Executes script from file"

    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }
    companion object {
        /**
         * Множество канонических путей к файлам, которые в данный момент выполняются.
         */
        private val executingScr = mutableSetOf<String>()
    }
    /**
     * Статический объект для отслеживания выполняемых скриптов.
     * Хранит множество путей к файлам, которые уже выполняются, чтобы избежать рекурсии.
     */


    /**
     * Выполняет команды из файла, указанного по пути [fileName].
     *
     * Алгоритм:
     * 1. Если [fileName] не указано, запрашивает путь у пользователя.
     * 2. Проверяет существование файла и права на чтение.
     * 3. Проверяет, не выполняется ли уже данный скрипт (для предотвращения рекурсии).
     * 4. Читает файл построчно и выполняет каждую команду с помощью [ce].
     * 5. Выводит сообщения об успехе или ошибках для каждой команды.
     *
     * @param fileName Путь к файлу со скриптом (может быть null, тогда путь запрашивается у пользователя).
     * @throws java.lang.Exception Если произошла ошибка при чтении файла или выполнении команды.
     */
    override fun execute(args: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun execute(fileName: String?) {

        val srcPath = fileName ?: run {
            outputManager.print("Введите путь к файлу: ")
            inputManager.read()
        }

        val file = File(srcPath)
        if (!file.exists()) {
            outputManager.println("Файл не найден.")
            return
        }

        val canonicalPath = file.canonicalPath
        if (executingScr.contains(canonicalPath)) {
            println("Скрипт уже выполняется.")
            return
        }

        executingScr.add(canonicalPath)

        if (!file.canRead()) {
            outputManager.println("Нет прав на чтение данного файла.")
            return
        }

        try {
            inputManager.startScriptRead(srcPath)
            var lineNumber = 0
            while (inputManager.isScriptMode()) {
                val line = inputManager.read()
                lineNumber++
                if (line.isEmpty()) {
                    if (!inputManager.isScriptMode()) break // Прерываем, если файл закончился
                    outputManager.println("Пустая строка пропущена (строка $lineNumber).")
                    continue
                }

                outputManager.println("Выполняется команда из скрипта (строка $lineNumber): $line")
                try {
                    commandInvoker.executeCommand(line)
                } catch (e: Exception) {
                    outputManager.println("${e.message}")
                }
            }
        } catch (e: Exception) {
            outputManager.println("Ошибка при чтении файла: ${e.message}")
            inputManager.finishScriptRead()
        }finally {

            executingScr.remove(canonicalPath)
        }
    }

    /**
     * Выполняет команду без аргументов.
     * Вызывает [execute] с параметром [fileName] равным null,
     * что приводит к запросу пути к файлу у пользователя.
     */

}