package org.example.commands.consoleCommands

import baseClasses.Coordinates
import baseClasses.Vehicle
import collection.CollectionManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.serverUtils.ConnectionManager
import org.example.serverUtils.Read
import utils.inputOutput.OutputManager
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper
import javax.lang.model.element.NestingKind

/**
 * Команда для подсчёта количества транспортных средств с мощностью двигателя больше заданного значения.
 * Использует [collection.CollectionManager] для доступа к коллекции и [org.example.serverUtils.Read] для чтения данных от пользователя.
 * Сравнение выполняется с помощью метода [baseClasses.Vehicle.compareTo].
 *
 * @property cm Менеджер коллекции, содержащий список транспортных средств.
 * @property rm Объект для чтения данных от пользователя.
 * @constructor Создаёт команду [CountGrThanEngPwCommand] с заданными менеджерами [cm] и [rm].
 */
@Serializable
class CountGrThanEngPwCommand(
    @Transient val cm: CollectionManager = null!!,
    @Transient private val rm: Read = null!!,
    @Transient private val outputManager: OutputManager = null!!,
    @Transient private val connectionManager: ConnectionManager = null!!
) : Command {
    override val interactive = true
    private var argsType = mapOf(
        "engPw" to "Float"
    )
    private val info = "Return a quantity of objects, which have engPw greater than given"

    /**
     * Выполняет команду подсчёта количества объектов с мощностью двигателя больше заданного значения.
     *
     * Алгоритм:
     * 1. Если коллекция пуста, выводит сообщение и завершает выполнение.
     * 2. Получает значение мощности двигателя из аргумента [enginePowerStr] или запрашивает его у пользователя через [rm].
     * 3. Создаёт временный объект [baseClasses.Vehicle] с указанной мощностью для сравнения.
     * 4. Подсчитывает количество объектов в коллекции, у которых мощность больше заданной.
     * 5. Выводит результат.
     *
     * @param enginePowerStr Строковое представление мощности двигателя для сравнения (может быть null).
     */

    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }

    override fun execute(args: Map<String, String>, username: String) {
        if (cm.getCollection().isEmpty()) {
            val response = ResponseWrapper(ResponseType.OK, "hueta", receiver = args["sender"]!!)
            connectionManager.send(response)
            return
        }
        val engPower = args["engPw"]?.toFloatOrNull()

        val element = Vehicle(
            name = "ComparingModel",
            coordinates = Coordinates(0, 0),
            enginePower = engPower,
            capacity = 0.0f,
            distanceTravelled = 0,
            fuelType = null
        )

        val count = cm.getCollection().filter { it > element }.size
        val response = ResponseWrapper(ResponseType.OK, "Кол-во объектов enginePower которых больше $engPower -> $count", receiver = args["sender"]!!)
        connectionManager.send(response)
    }
}