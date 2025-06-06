package org.example.commands.consoleCommands

import baseClasses.Coordinates
import baseClasses.Vehicle
import collection.CollectionManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.serverUtils.ConnectionManager
import org.example.serverUtils.Read
import utils.inputOutput.OutputManager
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

/**
 * Команда для удаления всех транспортных средств с мощностью двигателя больше заданного значения.
 * Использует [collection.CollectionManager] для управления коллекцией и [org.example.serverUtils.Read] для чтения данных от пользователя.
 * Сравнение выполняется с помощью метода [baseClasses.Vehicle.compareTo].
 *
 * @property cm Менеджер коллекции, содержащий список транспортных средств.
 * @property r Объект для чтения данных от пользователя.
 * @constructor Создаёт команду [RemoveGreaterCommand] с заданными менеджерами [cm] и [r].
 */
@Serializable
class RemoveGreaterCommand(
    @Transient private val cm: CollectionManager = null!!,
    @Transient private val r: Read = null!!,
    @Transient private val outputManager: OutputManager = null!!,
    @Transient private val connectionManager: ConnectionManager = null!!
) : Command {

    override val interactive = true
    private val argsType = mapOf(
        "engPw" to "Float"
    )
    private val info = "Removes all items from collection that have greater engPW than given"

    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }

    /**
     * Выполняет команду удаления всех транспортных средств с мощностью двигателя больше заданного значения.
     *
     * Алгоритм:
     * 1. Если коллекция пуста, выводит сообщение и завершает выполнение.
     * 2. Получает значение мощности двигателя из аргумента [enginePowerStr] или запрашивает его у пользователя через [r].
     * 3. Создаёт временный объект [baseClasses.Vehicle] с указанной мощностью для сравнения.
     * 4. Находит все элементы в коллекции, у которых мощность больше заданной.
     * 5. Удаляет найденные элементы из коллекции и из списка использованных ID в [baseClasses.Vehicle.Companion.existingIds].
     * 6. Выводит количество удалённых элементов и информацию о каждом удалённом объекте.
     *
     * @param enginePowerStr Строковое представление мощности двигателя для сравнения (может быть null).
     */
    override fun execute(request: RequestWrapper, username: String): ResponseWrapper {
        var response: ResponseWrapper
        if(!(cm.getCollection().isEmpty())){
            val enginePower = request.args["engPw"]?.toFloatOrNull()?: return ResponseWrapper(ResponseType.ERROR, "Engine power must be a number", receiver = username)
            var i = 0
            val element = Vehicle(
                name = "ComparingModel",
                coordinates = Coordinates(0, 0),
                enginePower = enginePower,
                capacity = 0.0f,
                distanceTravelled = 0,
                fuelType = null
            )
            val vehiclesToRemove = cm.getCollection()
                .filter { vehicle ->
                    vehicle.enginePower ?: Float.MIN_VALUE > enginePower &&
                            cm.getRelationship()[vehicle.getId()] == username
                }

            if (vehiclesToRemove.isEmpty()) {
                response = ResponseWrapper(ResponseType.OK, "goida can't delete anything", receiver = username)
                return response
            }

            vehiclesToRemove.forEach { vehicle ->
                cm.removeVehicleById(vehicle.getId(), username, vehicle)
                i++
            }

            val result = ResponseWrapper(ResponseType.OK, "Deleted $i govno/(a)", receiver = username)
            return result

        }else{
            response = ResponseWrapper(ResponseType.OK, "goida", receiver = username)
            return response
        }
    }
}