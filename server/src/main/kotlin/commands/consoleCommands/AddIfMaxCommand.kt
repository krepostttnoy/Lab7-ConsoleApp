package org.example.commands.consoleCommands

import baseClasses.Vehicle
//import baseClasses.withNewId
import collection.CollectionManager
import org.example.commands.consoleCommands.Command
import console.IVehicleManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.serverUtils.ConnectionManager
import utils.JsonCreator
import utils.inputOutput.OutputManager
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

/**
 * Команда для добавления нового транспортного средства в коллекцию, если оно больше максимального.
 * Сравнение выполняется по мощности двигателя ([Vehicle.enginePower]) с помощью [Vehicle.compareTo].
 * Использует [console.IVehicleManager] для создания нового объекта [Vehicle] и [collection.CollectionManager] для работы с коллекцией.
 *
 * @property cm Менеджер коллекции, в которую может быть добавлено транспортное средство.
 * @property vm Менеджер для создания нового транспортного средства.
 * @constructor Создаёт команду [AddIfMaxCommand] с заданными менеджерами [cm] и [vm].
 */
@Serializable
class AddIfMaxCommand(
    @Transient private val cm: CollectionManager = null!!,
    @Transient private val vm: IVehicleManager = null!!,
    @Transient private val outputManager: OutputManager = null!!,
    @Transient private val connectionManager: ConnectionManager = null!!
) : Command {
    override val interactive = true
    private val argsType = mapOf(
        "vehicle" to "Vehicle"
    )
    private val info = "Adds a new vehicle to collection if it's max"

    /**
     * Выполняет команду добавления нового транспортного средства, если оно больше максимального.
     *
     * Алгоритм:
     * 1. Если коллекция пуста, добавляет новый объект и завершает выполнение.
     * 2. Находит транспортное средство с максимальной мощностью двигателя в коллекции.
     * 3. Создаёт новый объект [Vehicle] с помощью [vm].
     * 4. Если новый объект больше максимального (по [Vehicle.compareTo]), добавляет его в коллекцию.
     * 5. Выводит сообщение о результате операции.
     */
    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }


    override fun execute(request: RequestWrapper, username: String): ResponseWrapper {
        try{
            val jsonCreator = JsonCreator()
            if (cm.getCollection().isEmpty()) {
                outputManager.println("Коллекция пуста.")
                val vehicleJson = request.args["vehicle"] ?: throw IllegalArgumentException("Vehicle data is missing")
                val vehicle = jsonCreator.stringToObject<Vehicle>(vehicleJson)
//                if (cm.getCollection().any { it.getId() == vehicle.getId()}) {
//                    throw IllegalArgumentException("Vehicle with ID ${vehicle.getId()} already exists!")
//                }
                cm.addVehicle(vehicle, username)
                val response = ResponseWrapper(ResponseType.OK, "Vehicle added: ${vehicle.name}", receiver = username)
                return response
            }

            val maxVehicle = cm.getCollection().maxWithOrNull(compareBy { it.enginePower })
            if (maxVehicle == null) {
                return ResponseWrapper(
                    ResponseType.ERROR,
                    "Cannot compare vehicles: no valid enginePower values found in the collection.",
                    receiver = username
                )
            }
            val vehicleJson = request.args["vehicle"] ?: throw IllegalArgumentException("Vehicle data is missing")
            val newVehicle = jsonCreator.stringToObject<Vehicle>(vehicleJson)
//            if (cm.getCollection().any { it.getId() == newVehicle.getId() }) {
//                throw IllegalArgumentException("Vehicle with ID ${newVehicle.getId()} already exists!")
//            }

            if (newVehicle.compareTo(maxVehicle) > 0) {
                cm.addVehicle(newVehicle, username)
                val response = ResponseWrapper(ResponseType.OK, "Vehicle added: ${newVehicle.name}", receiver = username)
                return response
            } else {
                val response = ResponseWrapper(ResponseType.OK, "Vehicle can't be added: ${newVehicle.name}", receiver = username)
                return response
            }

        }catch (e: Exception){
            val response = ResponseWrapper(ResponseType.ERROR, "Error: ${e.message}", receiver = username)
            return response
        }
    }
}