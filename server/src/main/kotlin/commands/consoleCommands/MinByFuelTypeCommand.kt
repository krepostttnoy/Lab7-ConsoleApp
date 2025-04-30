package org.example.commands.consoleCommands

import baseClasses.FuelType
import collection.CollectionManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.serverUtils.ConnectionManager
import utils.inputOutput.OutputManager
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

/**
 * Команда для поиска транспортного средства с минимальным значением [baseClasses.FuelType].
 * Использует [collection.CollectionManager] для доступа к коллекции и поиска элемента.
 * Сравнение выполняется на основе естественного порядка значений [baseClasses.FuelType].
 *
 * @property cm Менеджер коллекции, содержащий список транспортных средств.
 * @constructor Создаёт команду [MinByFuelTypeCommand] с заданным менеджером [cm].
 */
@Serializable
class MinByFuelTypeCommand(
    @Transient private val cm: CollectionManager = null!!,
    @Transient private val outputManager: OutputManager = null!!,
    @Transient private val connectionManager: ConnectionManager = null!!
    ) : Command {
    override val interactive = false
    private val argsType = emptyMap<String, String>()
    private val info = "Returns object that is min by fuel type"

    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }

    /**
     * Выполняет команду поиска транспортного средства с минимальным значением [baseClasses.FuelType].
     *
     * Алгоритм:
     * 1. Если коллекция пуста, выводит сообщение и завершает выполнение.
     * 2. Ищет транспортное средство с минимальным [baseClasses.FuelType], заменяя null на минимальное значение [baseClasses.FuelType].
     * 3. Если элемент найден, выводит его имя и тип топлива.
     * 4. Если элемент не найден, выводит сообщение об ошибке.
     */
    override fun execute(args: Map<String, String>) {
        if (!(cm.getCollection().isEmpty())) {
            val minVehicle = cm.getCollection().minByOrNull { vehicle ->
                vehicle.fuelType ?: FuelType.entries.toTypedArray().min()
            }
            if (minVehicle == null) {
                outputManager.println("Не удалось найти элемент с минимальным fuelType.")
                return
            }
            val response = ResponseWrapper(ResponseType.OK, "${minVehicle.name} -> ${minVehicle.fuelType}")
            connectionManager.send(response)
        }else{
            val response = ResponseWrapper(ResponseType.OK, "Collection is empty")
            connectionManager.send(response)
            return
        }
    }
}