package org.example.commands.consoleCommands

import baseClasses.Coordinates
import baseClasses.Vehicle
import baseClasses.withNewId
import collection.CollectionManager
import console.IVehicleManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.serverUtils.ConnectionManager
import utils.JsonCreator
import utils.inputOutput.OutputManager
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

/**
 * Команда для добавления нового транспортного средства в коллекцию.
 * Использует [console.IVehicleManager] для создания нового объекта [Vehicle] и добавляет его в коллекцию через [collection.CollectionManager].
 *
 * @property cm Менеджер коллекции, в которую будет добавлено транспортное средство.
 * @property vm Менеджер для создания нового транспортного средства.
 * @constructor Создаёт команду [AddCommand] с заданными менеджерами [cm] и [vm].
 */
@Serializable
class AddCommand(
    @Transient private val cm: CollectionManager = null!!,
    @Transient private val vm: IVehicleManager = null!!,
    @Transient private val outputManager: OutputManager = null!!,
    @Transient private val connectionManager: ConnectionManager = null!!
) : Command {
    override val interactive = true

    override fun execute(args: Map<String, String>, username: String) {
        try{
            val jsonCreator = JsonCreator()
            val vehicleJson = args["vehicle"] ?: throw IllegalArgumentException("Vehicle data is missing")
            val vehicle = jsonCreator.stringToObject<Vehicle>(vehicleJson).withNewId()

            if (cm.getCollection().any { it.getId() == vehicle.getId() }) {
                throw IllegalArgumentException("Vehicle with ID ${vehicle.getId()} already exists!, ${Vehicle.existingIds.toList()}")
            }
            cm.addVehicle(vehicle, username)
            val response = ResponseWrapper(ResponseType.OK, "Vehicle added: ${vehicle.name}", receiver = args["sender"]!!)
            connectionManager.send(response)
        }catch (e: Exception){
            val response = ResponseWrapper(ResponseType.ERROR, "Error: ${e.message}", receiver = args["sender"]!!)
            connectionManager.send(response)
        }
    }

    private val argsType = mapOf(
        "vehicle" to "Vehicle"
    )
    private val info = "Adds a new vehicle to the collection"



    /**
     * Выполняет команду добавления нового транспортного средства.
     * Создаёт новый объект [Vehicle] с помощью [vm] и добавляет его в коллекцию через [cm].
     * Выводит сообщение об успешном добавлении или об ошибке.
     *
     * @throws IllegalArgumentException Если создание или добавление объекта [Vehicle] не удалось.
     */
    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }
}