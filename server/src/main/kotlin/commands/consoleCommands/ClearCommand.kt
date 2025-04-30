package org.example.commands.consoleCommands

import baseClasses.Vehicle
import collection.CollectionManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.commands.consoleCommands.Command
import org.example.serverUtils.ConnectionManager
import utils.inputOutput.OutputManager
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

/**
 * Команда для очистки коллекции транспортных средств.
 * Удаляет все элементы из коллекции, управляемой [collection.CollectionManager], и очищает список использованных идентификаторов в [baseClasses.Vehicle].
 *
 * @property cm Менеджер коллекции, который будет очищен.
 * @constructor Создаёт команду [ClearCommand] с заданным менеджером [cm].
 */
@Serializable
class ClearCommand(
    @Transient private val cm: CollectionManager = null!!,
    @Transient val outputManager: OutputManager = null!!,
    @Transient private val connectionManager: ConnectionManager = null!!
    ) : Command {
    override val interactive = false
    private val argsType = emptyMap<String, String>()
    private val info = "Clears the collection"

    /**
     * Выполняет команду очистки коллекции.
     *
     * Алгоритм:
     * 1. Если коллекция пуста, выводит сообщение и завершает выполнение.
     * 2. Удаляет все элементы из коллекции с помощью [CollectionManager].
     * 3. Очищает список использованных идентификаторов в [baseClasses.Vehicle.Companion.existingIds].
     * 4. Выводит сообщение об успешной очистке и текущий размер коллекции.
     */
    override fun execute(args: Map<String, String>) {
        if (cm.getCollection().isEmpty()) {
            val response = ResponseWrapper(ResponseType.OK, "Collection is empty")
            connectionManager.send(response)
            return
        }

        cm.clear()
        Vehicle.Companion.existingIds.clear()
        val response = ResponseWrapper(ResponseType.OK, "Collection is cleared. Size: ${cm.getCollection().size}")
        connectionManager.send(response)
    }

    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }
}