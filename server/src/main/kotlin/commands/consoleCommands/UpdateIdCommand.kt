package org.example.commands.consoleCommands

import baseClasses.Coordinates
import baseClasses.FuelType
import baseClasses.Vehicle
import collection.CollectionManager
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.serverUtils.ConnectionManager
import org.example.serverUtils.Reader
import org.example.serverUtils.Validator
import utils.JsonCreator
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

/**
 * Команда для обновления данных транспортного средства в коллекции по его идентификатору.
 * Использует [collection.CollectionManager] для управления коллекцией, [org.example.serverUtils.Validator] для чтения данных от пользователя
 * и [org.example.serverUtils.Reader] для ввода новых значений полей.
 *
 * @property cm Менеджер коллекции, содержащий список транспортных средств.
 * @property console Объект для чтения и валидации данных из консоли.
 * @constructor Создаёт команду [UpdateIdCommand] с заданным менеджером [cm].
 */
@Serializable
class UpdateIdCommand(
    @Transient private val cm: CollectionManager = null!!,
    @Transient private val outputManager: OutputManager = null!!,
    @Transient private val inputManager: InputManager = null!!,
    @Transient private val connectionManager: ConnectionManager = null!!
    ) : Command {
    override val interactive = true
    private val argsType = mapOf(
        "id" to "Int",
        "field" to "String",
        "value" to "String"
    )
    private val info = "Updates a field from item with given id"

    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }
    /**
     * Объект для чтения и валидации данных из консоли.
     */
    @Transient
    val console = Validator(outputManager, inputManager)

    /**
     * Выполняет команду обновления данных транспортного средства по указанному идентификатору.
     *
     * Алгоритм:
     * 1. Если коллекция пуста, выводит сообщение и завершает выполнение.
     * 2. Выводит список доступных элементов с их ID и именами.
     * 3. Запрашивает у пользователя ID элемента для обновления.
     * 4. Если ID некорректен или элемент не найден, выводит сообщение об ошибке.
     * 5. Запрашивает у пользователя поле для обновления (name, coordinates, enginePower, capacity, distanceTravelled, fuelType).
     * 6. Обновляет указанное поле с помощью [org.example.serverUtils.Reader], создавая копию объекта [baseClasses.Vehicle] с новым значением.
     * 7. Заменяет старый элемент в коллекции на обновлённый.
     */
    override fun execute(args: Map<String, String>) {
        if (cm.getCollection().isEmpty()) {
            val response = ResponseWrapper(ResponseType.OK, "Collection is empty")
            connectionManager.send(response)
            return
        }

        val idStr = args["id"] ?: run {
            val response = ResponseWrapper(ResponseType.ERROR, "Missing id argument")
            connectionManager.send(response)
            return
        }

        val id = idStr.toIntOrNull() ?: run {
            val response = ResponseWrapper(ResponseType.ERROR, "Invalid id: $idStr")
            connectionManager.send(response)
            return
        }

        val field = args["field"] ?: run {
            val response = ResponseWrapper(ResponseType.ERROR, "Missing field argument")
            connectionManager.send(response)
            return
        }

        val value = args["value"] ?: run {
            val response = ResponseWrapper(ResponseType.ERROR, "Missing value argument")
            connectionManager.send(response)
            return
        }

        val index = cm.getCollection().indexOfFirst { it.id == id }
        if (index == -1) {
            val response = ResponseWrapper(ResponseType.ERROR, "Element with ID = $id does not exist")
            connectionManager.send(response)
            return
        }

        val oldVehicle = cm.getCollection()[index]
        Vehicle.Companion.removeId(oldVehicle.id)
        val jsonCreator = JsonCreator()

        val newVehicle = when (field.lowercase()) {
            "name" -> oldVehicle.copy(name = value)
            "coordinates", "coords" -> {
                val coordinates = jsonCreator.stringToObject<Coordinates>(value)
                oldVehicle.copy(coordinates = coordinates)
            }
            "enginepower", "ep" -> {
                val enginePower = value.toFloatOrNull() ?: run {
                    val response = ResponseWrapper(ResponseType.ERROR, "Invalid enginePower: $value")
                    connectionManager.send(response)
                    return
                }
                oldVehicle.copy(enginePower = enginePower)
            }
            "capacity", "cap" -> {
                val capacity = value.toFloatOrNull() ?: run {
                    val response = ResponseWrapper(ResponseType.ERROR, "Invalid capacity: $value")
                    connectionManager.send(response)
                    return
                }
                oldVehicle.copy(capacity = capacity)
            }
            "distancetravelled", "dt" -> {
                val distanceTravelled = value.toIntOrNull() ?: run {
                    val response = ResponseWrapper(ResponseType.ERROR, "Invalid distanceTravelled: $value")
                    connectionManager.send(response)
                    return
                }
                oldVehicle.copy(distanceTravelled = distanceTravelled)
            }
            "fueltype", "ft" -> {
                val fuelType = jsonCreator.stringToObject<FuelType?>(value)
                oldVehicle.copy(fuelType = fuelType)
            }
            else -> {
                val response = ResponseWrapper(ResponseType.ERROR, "Invalid field: $field. Available fields: name, coordinates, enginePower, capacity, distanceTravelled, fuelType")
                connectionManager.send(response)
                return
            }
        }

        cm.updateVehicleAt(index, newVehicle)
        val response = ResponseWrapper(ResponseType.OK, "Element with ID = $id updated successfully")
        connectionManager.send(response)
    }
}