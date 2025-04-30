package org.example.commands.consoleCommands

import collection.CollectionManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.serverUtils.ConnectionManager
import utils.inputOutput.OutputManager
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

/**
 * Команда для отображения всех элементов коллекции.
 * Использует [collection.CollectionManager] для получения строкового представления коллекции и вывода его в консоль.
 *
 * @property cm Менеджер коллекции, содержащий список транспортных средств.
 * @constructor Создаёт команду [ShowCommand] с заданным менеджером [cm].
 */
@Serializable
class ShowCommand(
    @Transient private val cm: CollectionManager = null!!,
    @Transient private val outputManager: OutputManager = null!!,
    @Transient private val connectionManager: ConnectionManager = null!!
    ) : Command {
    override val interactive = false
    private val argsType = emptyMap<String, String>()
    private val info = "Returns info about all items from the collection"

    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }

    /**
     * Выполняет команду отображения элементов коллекции.
     * Вызывает [CollectionManager.printCollection] и выводит результат в консоль.
     */
    override fun execute(args: Map<String, String>) {
        val response = ResponseWrapper(ResponseType.OK, cm.printCollection())
        connectionManager.send(response)
    }
}