package org.example.commands.consoleCommands

import baseClasses.Vehicle
import collection.CollectionManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.serverUtils.ConnectionManager
import org.example.serverUtils.Validator
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

/**
 * Команда для удаления транспортного средства из коллекции по его идентификатору.
 * Использует [collection.CollectionManager] для управления коллекцией и [org.example.serverUtils.Validator] для чтения данных от пользователя.
 *
 * @property cm Менеджер коллекции, содержащий список транспортных средств.
 * @constructor Создаёт команду [RemoveByIdCommand] с заданным менеджером [cm].
 */
@Serializable
class RemoveByIdCommand(
    @Transient private val cm: CollectionManager = null!!,
    @Transient private val outputManager: OutputManager = null!!,
    @Transient private val inputManager: InputManager = null!!,
    @Transient private val connectionManager: ConnectionManager = null!!
    ) : Command {
    override val interactive = true
    private val argsType = mapOf(
        "index" to "Int"
    )
    private val info = "Removes an object from the collection with given id"

    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }

    /**
     * Выполняет команду удаления транспортного средства по указанному идентификатору.
     *
     * Алгоритм:
     * 1. Если коллекция пуста, выводит сообщение и завершает выполнение.
     * 2. Если [idStr] не указано, выводит список текущих элементов с их ID.
     * 3. Получает идентификатор из аргумента [idStr] или запрашивает его у пользователя через [org.example.serverUtils.Validator].
     * 4. Находит элемент с указанным ID в коллекции.
     * 5. Если элемент найден, удаляет его из коллекции и из списка использованных ID в [baseClasses.Vehicle].
     * 6. Выводит сообщение об успешном удалении или об ошибке, если элемент не найден.
     *
     * @param idStr Строковое представление идентификатора элемента для удаления (может быть null).
     */
    override fun execute(args: Map<String, String>) {
        if (!(cm.getCollection().isEmpty())) {
            val id = args["index"]?.toInt()

            val index = cm.getCollection().indexOfFirst { it.id == id }
            if (index == -1) {
                val response = ResponseWrapper(ResponseType.OK, "Элемента с ID = $id не существует.")
                connectionManager.send(response)
                return
            }

            val vehicleToRemove = cm.getCollection()[index]
            cm.removeVehicle("removeAt", index, null)
            Vehicle.Companion.existingIds.remove(vehicleToRemove.id)

            val response = ResponseWrapper(ResponseType.OK, "")
            connectionManager.send(response)
        }else{
            val response = ResponseWrapper(ResponseType.OK, "Collection is empty")
            connectionManager.send(response)
            return
        }
    }
}