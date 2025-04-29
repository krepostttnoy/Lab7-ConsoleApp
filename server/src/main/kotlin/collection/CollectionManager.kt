package collection

import baseClasses.Vehicle
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.serverUtils.ConnectionManager
import utils.inputOutput.OutputManager
import java.text.Collator
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Collections


/**
 * Класс для управления коллекцией транспортных средств.
 * Хранит коллекцию объектов типа [Vehicle], предоставляет методы для работы с ней,
 * а также информацию о дате инициализации коллекции.
 *
 * @property baseCollection Список транспортных средств, хранимых в коллекции.
 * @property initializationDate Дата и время инициализации коллекции, устанавливается автоматически при создании объекта.
 * @constructor Создаёт пустую коллекцию транспортных средств с текущей датой инициализации.
 */

class CollectionManager {

    /**
     * Список транспортных средств, хранимых в коллекции.
     */
    val outputManager = OutputManager()
    val baseCollection = Collections.synchronizedList(ArrayList<Vehicle>())
    val connectionManager = ConnectionManager()

    private val initializationDate: LocalDate = LocalDate.now()

    /**
     * Возвращает коллекцию транспортных средств.
     *
     * @return Список [ArrayList] с объектами типа [Vehicle].
     */
    fun getCollection(): List<Vehicle> {
        return baseCollection.toList()
    }

    /**
     * Сортирует коллекцию по мощности двигателя транспортных средств.
     * Использует метод [sort] из [ArrayList], который вызывает [Vehicle.compareTo].
     */
    fun sortCollectionByEnginePower() {
        baseCollection.sort()
    }

    /**
     * Возвращает строковое представление всей коллекции.
     * Каждый элемент коллекции представлен в виде строки, полученной через [Vehicle.toString].
     *
     * @return Строковое представление коллекции, где элементы разделены переносом строки.
     */
    fun printCollection(): String {
        return baseCollection.joinToString("\n") { it.toString() }
    }

    /**
     * Добавляет новое транспортное средство в коллекцию.
     *
     * @param vehicle Объект [Vehicle], который нужно добавить в коллекцию.
     */
    fun addVehicle(vehicle: Vehicle) {
            baseCollection.add(vehicle)

    }

    /**
     * Выводит информацию о коллекции в консоль.
     * Включает тип коллекции, дату инициализации и количество элементов.
     */
    fun printCollectionInfo(): String {
        return "Информация о коллекции:\n" +
                "Тип: ${baseCollection.javaClass.simpleName}<${Vehicle::class.simpleName}>\n" +
                "Дата инициализации: $initializationDate\n" +
                "Кол-во элементов: ${baseCollection.size}"
    }
}