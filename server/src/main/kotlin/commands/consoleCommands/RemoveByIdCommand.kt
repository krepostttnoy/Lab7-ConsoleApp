package org.example.commands.consoleCommands

import baseClasses.Vehicle
import collection.CollectionManager
import com.fasterxml.jackson.databind.JsonSerializable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.serverUtils.ConnectionManager
import org.example.serverUtils.Validator
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager
import utils.wrappers.RequestWrapper
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
    override fun execute(request: RequestWrapper, username: String): ResponseWrapper {
        if (!(cm.getCollection().isEmpty())) {

            val id = request.args["index"]?.toIntOrNull()
                ?: return ResponseWrapper(ResponseType.ERROR, "ID must be a number", receiver = username)

            val index = cm.getCollection().indexOfFirst { it.getId() == id }
            if (index == -1) {
                val response = ResponseWrapper(ResponseType.OK, "Элемента с ID = $id не существует.", receiver = username)
                return response
            }

            val vehicleToRemove = cm.getCollection()[index]
            cm.removeVehicleById(id, username, vehicleToRemove)
            //Vehicle.Companion.existingIds.remove(vehicleToRemove.getId())

            val response = ResponseWrapper(ResponseType.OK, "", receiver = username)
            return response
        }else{
            val response = ResponseWrapper(ResponseType.OK, "Collection is empty", receiver = username)
            return response
        }
    }
}