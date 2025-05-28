package collection

import baseClasses.Coordinates
import baseClasses.FuelType
import baseClasses.Vehicle
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.dbConnect.DbManager
import utils.exceptions.UserIsNotAuthorizedException
import java.sql.SQLException
import java.time.LocalDate
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
    private val baseCollection = Collections.synchronizedList(ArrayList<Vehicle>())
    private val relationship = Collections.synchronizedMap(mutableMapOf<Int, String>())
    private val initializationDate: LocalDate = LocalDate.now()
    private val dbManager = DbManager("jdbc:postgresql://localhost:15432/studs", "s474305", "yWizzR0CBOadnGlk")
    private val logger: Logger = LogManager.getLogger(CollectionManager::class.java)


    fun clear(username: String) {
        synchronized(baseCollection) {
            try {
                val idsToRemove = baseCollection
                    .filter { dbManager.userOwned(it.getId(), username) }
                    .map { it.getId() }

                dbManager.deleteVehicles(idsToRemove)


                idsToRemove.forEach { id ->
                    baseCollection.removeIf { it.getId() == id }
                    relationship.remove(id)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun updateVehicleAt(id: Int, newVehicle: Vehicle, username: String) {
        synchronized(baseCollection) {
            val vehicle = baseCollection.firstOrNull { it.getId() == id }
                ?: throw NoSuchElementException("Vehicle with ID $id not found")

            if (relationship[vehicle.getId()] != username) {
                throw SecurityException("User $username doesn't own vehicle ${vehicle.getId()}")
            }

            dbManager.updateVehicle(vehicle.getId(), newVehicle, username)
            baseCollection.replaceAll { if (it.getId() == id) newVehicle else it }
        }
    }

    /**
     * Возвращает коллекцию транспортных средств.
     *
     * @return Список [ArrayList] с объектами типа [Vehicle].
     */
    fun getCollection(): List<Vehicle> {
        synchronized(baseCollection) {
            return baseCollection.toList()
        }
    }

    fun getRelationship(): MutableMap<Int, String> {
        return relationship
    }

    /**
     * Возвращает строковое представление всей коллекции.
     * Каждый элемент коллекции представлен в виде строки, полученной через [Vehicle.toString].
     *
     * @return Строковое представление коллекции, где элементы разделены переносом строки.
     */
    fun printCollection(): String {
        synchronized(baseCollection) {
            return baseCollection.joinToString("\n") { it.toString() }
        }
    }

    /**
     * Добавляет новое транспортное средство в коллекцию.
     *
     * @param vehicle Объект [Vehicle], который нужно добавить в коллекцию.
     */
    fun addVehicle(vehicle: Vehicle, username: String) {
        synchronized(baseCollection) {
            dbManager.saveVehicle(vehicle, username)
            baseCollection.add(vehicle)
            relationship[vehicle.getId()] = username
            println(relationship)
        }
    }

    fun removeVehicle(argsName: String, argsRaw: Int?, vehicle: Vehicle?, username: String) {
        synchronized(baseCollection) {
            val vehicleId = vehicle?.getId() ?: throw IllegalArgumentException("Vehicle cannot be null")

            if (relationship[vehicleId] != username && username != "admin") {
                throw SecurityException("User $username is not authorized to modify vehicle $vehicleId")
            }

            when (argsName) {
                "removeAt" -> {
                    println("2454255")
                    val index = argsRaw ?: throw IllegalArgumentException("Index cannot be null")
                    if (index !in baseCollection.indices) throw IndexOutOfBoundsException("Invalid index: $index")

                    val removedVehicle = baseCollection.removeAt(index)
                    dbManager.deleteVehicle(removedVehicle.getId())
                    relationship.remove(removedVehicle.getId())
                    baseCollection.remove(removedVehicle)
                }
                "remove" -> {
                    if (!baseCollection.remove(vehicle)) {
                        throw NoSuchElementException("Vehicle not found in collection")
                    }
                    dbManager.deleteVehicle(vehicleId)
                    relationship.remove(vehicleId) // Удаляем связь
                }
                else -> throw IllegalArgumentException("Unknown operation: $argsName")
            }
        }
    }

    fun removeVehicleAt(index: Int, username: String) {
        synchronized(baseCollection) {
            if (index < 0 || index >= baseCollection.size) {
                throw IndexOutOfBoundsException("Index $index out of bounds")
            }

            val vehicle = baseCollection[index]

            if(relationship[vehicle.getId()] == username){
                baseCollection.removeAt(index)
                dbManager.deleteVehicle(vehicle.getId())
                relationship.remove(vehicle.getId())
            }else{
                throw IllegalArgumentException("Not allowed")
            }
        }
    }


    fun removeVehicleById(id: Int, username: String, vehicle: Vehicle){
        synchronized(baseCollection) {
            if(relationship[id] == username){
                dbManager.deleteVehicle(id)
                baseCollection.remove(vehicle)
                relationship.remove(id, username)
            }else{
                throw IllegalArgumentException("Not allowed")
            }
        }
    }
    /**
     * Выводит информацию о коллекции в консоль.
     * Включает тип коллекции, дату инициализации и количество элементов.
     */
    fun printCollectionInfo(): String {
        synchronized(baseCollection) {
            return "Информация о коллекции:\n" +
                    "Тип: ${baseCollection.javaClass.simpleName}<${Vehicle::class.simpleName}>\n" +
                    "Дата инициализации: $initializationDate\n" +
                    "Кол-во элементов: ${baseCollection.size}"
        }
    }
}