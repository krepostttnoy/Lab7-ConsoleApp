package baseClasses

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import serializers.DateSerializer
import java.util.Collections
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

/**
 * Класс, представляющий транспортное средство.
 * Содержит информацию о названии, координатах, мощности двигателя, вместимости, пройденном расстоянии и типе топлива.
 * Реализует интерфейс [Comparable] для сравнения транспортных средств по мощности двигателя.
 *
 * @property name Название транспортного средства.
 * @property coordinates Координаты транспортного средства, представленные в виде объекта [Coordinates].
 * @property enginePower Мощность двигателя (может быть null).
 * @property capacity Вместимость транспортного средства.
 * @property distanceTravelled Пройденное расстояние.
 * @property fuelType Тип топлива, представленный перечислением [FuelType] (может быть null).
 * @property id Уникальный идентификатор транспортного средства, генерируется автоматически.
 * @property creationDate Дата создания объекта, автоматически устанавливается при создании.
 * @constructor Создаёт объект [Vehicle] с заданными параметрами.
 */
@Serializable
data class Vehicle(
    var name: String,
    var coordinates: Coordinates,
    var enginePower: Float?,
    var capacity: Float,
    var distanceTravelled: Int,
    var fuelType: FuelType?
) : Comparable<Vehicle> {
    /**
     * Уникальный идентификатор транспортного средства.
     * Генерируется автоматически с помощью метода [generateId].
     */

    val id: Int = generateId()

    fun getId(): Int{
        return id
    }

    /**
     * Дата создания транспортного средства.
     * Устанавливается автоматически при создании объекта.
     */
    @Serializable(with = DateSerializer::class)
    val creationDate: Date = Date()




    /**
     * Возвращает строковое представление объекта [Vehicle].
     * Включает все свойства объекта в удобочитаемом формате.
     *
     * @return Строка с информацией о транспортном средстве, например:
     * ```
     * Vehicle -> {
     * name - Car1,
     * id - 1,
     * creationDate - Wed Mar 05 12:00:00 2025,
     * coordinates - (10, 20),
     * enginePower - 150.0,
     * capacity - 500.0,
     * distanceTravelled - 1000,
     * fuelType - DIESEL
     * }
     * ```
     */
    override fun toString(): String {
        return "Vehicle -> {\n" + "name - $name, \n" +
                "id - $id, \n" +
                "creationDate - $creationDate, \n" +
                "coordinates - ($coordinates), \n" +
                "enginePower - $enginePower, \n" +
                "capacity - $capacity, \n" +
                "distanceTravelled - $distanceTravelled, \n" +
                "fuelType - $fuelType\n}"
    }

    /**
     * Сравнивает два объекта [Vehicle] по мощности двигателя ([enginePower]).
     * Если мощность двигателя у одного из объектов равна null, применяется специальная логика сравнения.
     *
     * @param other Объект [Vehicle], с которым выполняется сравнение.
     * @return
     * - 0, если мощности двигателей равны (или оба значения null),
     * - -1, если [enginePower] текущего объекта null, а у [other] — нет,
     * - 1, если [enginePower] у [other] null, а у текущего объекта — нет,
     * - результат сравнения [enginePower] в остальных случаях.
     */
    override fun compareTo(other: Vehicle): Int {
        return when {
            this.enginePower == null && other.enginePower == null -> 0
            this.enginePower == null -> -1
            other.enginePower == null -> 1
            else -> this.enginePower!!.compareTo(other.enginePower!!)
        }
    }

    /**
     * Статический объект для управления идентификаторами транспортных средств.
     * Хранит множество уже использованных идентификаторов и предоставляет методы для их генерации и удаления.
     */
    companion object {
        /**
         * Множество уже использованных идентификаторов.
         */
        val existingIds = mutableSetOf<Int>()

        /**
         * Генерирует новый уникальный идентификатор для транспортного средства.
         * Идентификатор начинается с 1 и увеличивается, пока не будет найден свободный.
         *
         * @return Новый уникальный идентификатор.
         */
        fun generateId(): Int {
            var newId = 1
            while (existingIds.contains(newId)) {
                newId += 1
            }
            existingIds.add(newId)
            return newId
        }



        /**
         * Удаляет идентификатор из множества использованных идентификаторов.
         *
         * @param id Идентификатор, который нужно удалить.
         */
        fun removeId(id: Int) {
            existingIds.remove(id)
        }
    }
}

fun Vehicle.withNewId(): Vehicle = Vehicle(
    name = this.name,
    coordinates = this.coordinates,
    enginePower = this.enginePower,
    capacity = this.capacity,
    distanceTravelled = this.distanceTravelled,
    fuelType = this.fuelType
)