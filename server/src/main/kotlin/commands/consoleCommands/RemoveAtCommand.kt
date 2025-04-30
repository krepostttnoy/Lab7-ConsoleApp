package org.example.commands.consoleCommands

import baseClasses.FuelType
import baseClasses.Vehicle
import collection.CollectionManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.serverUtils.ConnectionManager
import org.example.serverUtils.Read
import utils.inputOutput.OutputManager
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

/**
 * Команда для удаления транспортного средства из коллекции по указанному индексу.
 * Использует [collection.CollectionManager] для управления коллекцией и [org.example.serverUtils.Read] для чтения данных от пользователя.
 *
 * @property cm Менеджер коллекции, содержащий список транспортных средств.
 * @property rm Объект для чтения данных от пользователя.
 * @constructor Создаёт команду [RemoveAtCommand] с заданными менеджерами [cm] и [rm].
 */
@Serializable
class RemoveAtCommand(
    @Transient private val cm: CollectionManager = null!!,
    @Transient private val rm: Read = null!!,
    @Transient private val outputManager: OutputManager = null!!,
    @Transient private val connectionManager: ConnectionManager = null!!
) : Command {
    override val interactive = true
     private val argsType = mapOf(
        "index" to "Int"
    )
    private val info = "Removes an object from collection with given index"

    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }

    /**
     * Выполняет команду удаления транспортного средства по указанному индексу.
     *
     * @param indexStr Строковое представление индекса элемента для удаления (может быть null).
     */
    override fun execute(args: Map<String, String>) {
        if (!(cm.baseCollection.isEmpty())) {
            val index = args["index"]?.toInt()

            if (index == null || index < 0 || index >= cm.baseCollection.size) {
                val response = ResponseWrapper(ResponseType.OK, "Incorrect index")
                connectionManager.send(response)
                return
            }

            val vehicleToRemove = cm.baseCollection[index]
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