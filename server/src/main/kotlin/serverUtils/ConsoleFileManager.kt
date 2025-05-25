package org.example.serverUtils

import baseClasses.Coordinates
import baseClasses.FuelType
import baseClasses.Vehicle
import collection.CollectionManager
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.dbConnect.DbManager
import org.example.users.UserManager
import utils.JsonCreator

/**
 * Класс для управления чтением и записью коллекции транспортных средств в файл.
 * Реализует интерфейс [IFileManager], предоставляя методы для загрузки и сохранения данных в CSV-файл.
 * Использует [CollectionManager] для управления коллекцией.
 *
 * @property collectionManager Менеджер коллекции, содержащий список транспортных средств.
 * @property filePath Путь к файлу, используемому для чтения и записи данных.
 * @constructor Создаёт экземпляр [ConsoleFileManager] с заданным менеджером [collectionManager].
 * Загружает данные из файла, указанного в переменной окружения CSV_FILE_PATH, или использует путь по умолчанию.
 */
class ConsoleFileManager(
    private val collectionManager: CollectionManager,
    private val dbManager: DbManager
    ) : IFileManager {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private val logger: Logger = LogManager.getLogger(ConsoleFileManager::class.java)
    private val jsonCreator = JsonCreator()

    override fun loadCollection() {
        try {
            logger.info("Loading from DB")
            val collection = dbManager.loadCollection()

            for (element in collection) {
                val vehicle = jsonCreator.stringToObject<Vehicle>(element.key)
                val parent = element.value
                try {
                    collectionManager.addVehicle(vehicle, parent)
                    logger.info("Loaded ${vehicle.name}")
                } catch (e:Exception) {
                    logger.info(e.message.toString())
                }

            }
            logger.info("Loaded ${collectionManager.getCollection().size} elements successfully")

        } catch (e: Exception) {
            logger.warn(e.message.toString())
        }
    }

    override fun saveCollection(userManager: UserManager) {
        try {
            logger.info("Сохранение в базу данных")

            for (element in collectionManager.getCollection()) {
                val relation = collectionManager.getRelationship()
                val username = relation[element.getId()]!!
                dbManager.saveVehicle(element, username)
            }

            logger.info("Сохранено ${collectionManager.getCollection().size} успешно элементов")

        } catch (e: Exception) {
            logger.warn(e)
        }
    }


    /**
     * Инициализирует объект, загружая данные из файла.
     * Вызывает [loadFromFile] с путём, указанным в [filePath].
     */
//    init {
//        loadFromFile(filePath)
//    }

    /**
     * Возвращает путь к файлу, используемому для чтения и записи.
     *
     * @return Путь к файлу, указанный в [filePath].
     */
//    override fun getFilePath(): String = filePath

    /**
     * Загружает данные из указанного CSV-файла в коллекцию.
     * Каждая строка файла должна содержать данные о транспортном средстве в формате:
     * name,coordinateX,coordinateY,enginePower,capacity,distanceTravelled,fuelType.
     *
     * @param filePath Путь к файлу, из которого нужно загрузить данные.
     * @throws IllegalArgumentException Если произошла ошибка при чтении файла или данные некорректны.
     */
//    override fun loadFromFile(filePath: String) {
//        try {
//            val file = File(filePath)
//            if (file.exists()) {
//                inputManager.startScriptRead(filePath)
//                BufferedInputStream(FileInputStream(file)).use { inputStream ->
//                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
//                        var lineNumber = 1
//                        var line: String?
//                        while (reader.readLine().also { line = it } != null) {
//                            lineNumber++
//                            if (lineNumber == 2) continue
//
//                            val params = line!!.split(",").map { it.trim() }
//                            if (params.size != 7) {
//                                logger.warn("Строка $lineNumber пропущена. Кол-во элементов ${params.size} вместо 7")
//                                continue
//                            }
//
//                            try {
//
//                                val name = params[0]
//                                if (name.isBlank()){
//                                    logger.error("Name can't be empty")
//                                    throw IllegalArgumentException("Name can't be empty")
//                                }
//
//                                val coordinateX = params[1].toLongOrNull()
//                                    ?: throw NumberFormatException("coordinateX '${params[1]}' is not a Long")
//                                if (coordinateX <= -818) {
//                                    logger.error("X must be > -818")
//                                    throw IllegalArgumentException("X must be > -818")
//                                }
//
//                                val coordinateY = params[2].toLongOrNull()
//                                    ?: throw NumberFormatException("coordinateY '${params[2]}' is not a Long")
//                                if (coordinateY > 730) {
//                                    logger.error("Y must be <= 730")
//                                    throw IllegalArgumentException("Y must be <= 730")
//                                }
//
//                                val enginePowerRaw = params[3].toFloatOrNull()
//                                    ?: throw NumberFormatException("enginePower '${params[3]}' is not a Float")
//                                if (enginePowerRaw <= 0) {
//                                    logger.error("EngPower must be > 0")
//                                    throw IllegalArgumentException("EngPower must be > 0")
//                                }
//                                val enginePower = enginePowerRaw
//
//                                val capacity = params[4].toFloatOrNull()
//                                    ?: throw NumberFormatException("capacity '${params[4]}' is not a Float")
//                                if (capacity <= 0) {
//                                    logger.error("Capacity must be > 0")
//                                    throw IllegalArgumentException("Capacity must be > 0")
//                                }
//
//                                val distanceTravelled = params[5].toIntOrNull()
//                                    ?: throw NumberFormatException("distanceTravelled '${params[5]}' is not an Int")
//                                if (distanceTravelled < 0) {
//                                    logger.error("DistTravelled must be >= 0")
//                                    throw IllegalArgumentException("DistTravelled must be >= 0")
//                                }
//
//                                val fuelTypeString = params[6]
//                                val fuelType = when (fuelTypeString.lowercase()) {
//                                    "electricity", "электричество", "электро" -> FuelType.ELECTRICITY
//                                    "antimatter", "анти", "антиматерия" -> FuelType.ANTIMATTER
//                                    "diesel", "дизель", "диз" -> FuelType.DIESEL
//                                    else -> throw IllegalArgumentException(
//                                        "Некорректный тип топлива '$fuelTypeString'. " +
//                                                "Возможные типы: ${FuelType.entries.joinToString(", ") { it.description }}"
//                                    )
//                                }
//
//                                val vehicle = Vehicle(
//                                    name = name,
//                                    coordinates = Coordinates(coordinateX, coordinateY),
//                                    enginePower = enginePower,
//                                    capacity = capacity,
//                                    distanceTravelled = distanceTravelled,
//                                    fuelType = fuelType
//                                )
//                                collectionManager.addVehicle(vehicle)
//                            } catch (e: NumberFormatException) {
//                                logger.warn("Warning: Line $lineNumber skipped - ${e.message}")
//                            } catch (e: IllegalArgumentException) {
//                                logger.warn("Warning: Line $lineNumber skipped - ${e.message}")
//                            } catch (e: Exception) {
//                                logger.warn("Warning: Line $lineNumber skipped - invalid data: ${e.message}")
//                            }
//                        }
//                    }
//                }
//                inputManager.finishScriptRead()
//            } else {
//                logger.info("File $filePath not found. Starting with empty collection.")
//            }
//        } catch (e: Exception) {
//            logger.error("Error reading CSV file: ${e.message}")
//            throw IllegalArgumentException("Error reading CSV file: ${e.message}")
//        }
//    }

    /**
     * Сохраняет коллекцию транспортных средств в указанный CSV-файл.
     * Формат файла: name,coordinateX,coordinateY,enginePower,capacity,distanceTravelled,fuelType.
     *
     * @param filePath Путь к файлу, в который нужно сохранить данные.
     */
//    override fun saveToFile() {
//        try {
//            val file = File(filePath)
//            BufferedOutputStream(FileOutputStream(file)).use { outputStream ->
//                val title = "name,coordinateX,coordinateY,enginePower,capacity,distanceTravelled,fuelType\n"
//                outputStream.write(title.toByteArray())
//                val vehicles = collectionManager.getCollection()
//                var line: String
//                for (vehicle in vehicles) {
//                    line = "${vehicle.name},${vehicle.coordinates.x},${vehicle.coordinates.y}," +
//                            "${vehicle.enginePower},${vehicle.capacity},${vehicle.distanceTravelled}," +
//                            "${vehicle.fuelType?.description ?: ""}\n"
//                    outputStream.write(line.toByteArray(Charsets.UTF_8))
//                }
//            }
//        } catch (e: Exception) {
//            logger.error("Error occurred when tried to save the collection. ${e.message}")
//        }
//    }
//
    fun startAutoSave(interval: Long, userManager: UserManager){
        scheduler.scheduleAtFixedRate({
            synchronized(collectionManager.getCollection()) {
                try {
                    saveCollection(userManager)
                    logger.info("Collection saved automatically")
                }catch (e: Exception){
                    logger.error("${e.message}")
                }
            }
        }, interval, interval, TimeUnit.SECONDS)

    }

    fun stopAutoSave() {
        scheduler.shutdown()
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow()
            }
        } catch (e: InterruptedException) {
            scheduler.shutdownNow()
        }
    }
}