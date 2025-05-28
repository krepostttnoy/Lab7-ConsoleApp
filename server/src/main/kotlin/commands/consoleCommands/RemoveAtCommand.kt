package org.example.commands.consoleCommands

import baseClasses.FuelType
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
    override fun execute(request: RequestWrapper, username: String): ResponseWrapper {
        if (!(cm.getCollection().isEmpty())) {
            val index = request.args["index"]?.toInt()
            println(index)

            if (index == null || index < 0 || index >= cm.getCollection().size) {
                val response = ResponseWrapper(ResponseType.OK, "Incorrect index", receiver = username)
                return response
            }

            println("hueta")
            val vehicleToRemove = cm.getCollection()[index]
            println("hueta1")
            cm.removeVehicleAt(index, username)
            println("hueta2")
            //Vehicle.Companion.existingIds.remove(vehicleToRemove.getId())

            val response = ResponseWrapper(ResponseType.OK, "", receiver = username)
            return response
        }else{
            val response = ResponseWrapper(ResponseType.OK, "Collection is empty", receiver = username)
            return response
        }
    }
}