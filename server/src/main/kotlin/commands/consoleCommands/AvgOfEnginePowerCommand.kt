package org.example.commands.consoleCommands

import collection.CollectionManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.commands.consoleCommands.Command
import org.example.serverUtils.ConnectionManager
import utils.JsonCreator
import utils.inputOutput.OutputManager
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

/**
 * Команда для вычисления среднего значения мощности двигателя транспортных средств в коллекции.
 * Использует [collection.CollectionManager] для доступа к коллекции и вычисляет среднее значение
 * свойства [Vehicle.enginePower] для всех элементов.
 *
 * @property cm Менеджер коллекции, содержащий список транспортных средств.
 * @constructor Создаёт команду [AvgOfEnginePowerCommand] с заданным менеджером [cm].
 */
@Serializable
class AvgOfEnginePowerCommand(
    @Transient val cm: CollectionManager = null!!,
    @Transient val outputManager: OutputManager = null!!,
    @Transient private val connectionManager: ConnectionManager = null!!
    ) : Command {
    override val interactive = false
    private val argsType = emptyMap<String, String>()
    private val info = "Returns average engine power of the all collection"

    /**
     * Выполняет команду вычисления среднего значения мощности двигателя.
     *
     * Алгоритм:
     * 1. Если коллекция пуста, выводит сообщение и завершает выполнение.
     * 2. Вычисляет сумму всех значений [Vehicle.enginePower], заменяя null на 0.0.
     * 3. Делит сумму на количество элементов в коллекции.
     * 4. Выводит среднее значение, сумму и размер коллекции.
     */
    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }

    override fun execute(request: RequestWrapper, username: String): ResponseWrapper {
        if (cm.getCollection().isEmpty()) {
            val response = ResponseWrapper(ResponseType.OK, "Коллекция пуста.", receiver = username)
            return response
        }

        val size = cm.getCollection().size
        val sum = cm.getCollection().sumOf { vehicle ->
            (vehicle.enginePower ?: 0.0f).toDouble()
        }
        val response = ResponseWrapper(ResponseType.OK, "Avg: ${sum/size}. Sum -> $sum, size -> $size", receiver = username)
        return response
    }
}