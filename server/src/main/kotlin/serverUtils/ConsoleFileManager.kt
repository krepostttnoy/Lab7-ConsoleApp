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
    private val outputManager: OutputManager,
    private val inputManager: InputManager
    ) : IFileManager {

    /**
     * Путь к файлу для чтения и записи данных.
     * По умолчанию берётся из переменной окружения CSV_FILE_PATH, иначе используется "collection/CollectionInput.csv".
     */
    private val filePath: String = System.getenv("CSV_FILE_PATH") ?: run {
        println("Переменная окружения CSV_FILE_PATH не установлена. Используется значение по умолчанию 'CollectionInput.csv'")
        "/Users/mark/Programming/languages/Kotlin/Lab6/server/src/main/kotlin/CollectionInput.csv"
    }

    /**
     * Инициализирует объект, загружая данные из файла.
     * Вызывает [loadFromFile] с путём, указанным в [filePath].
     */
    init {
        loadFromFile(filePath)
    }

    /**
     * Возвращает путь к файлу, используемому для чтения и записи.
     *
     * @return Путь к файлу, указанный в [filePath].
     */
    override fun getFilePath(): String = filePath

    /**
     * Загружает данные из указанного CSV-файла в коллекцию.
     * Каждая строка файла должна содержать данные о транспортном средстве в формате:
     * name,coordinateX,coordinateY,enginePower,capacity,distanceTravelled,fuelType.
     *
     * @param filePath Путь к файлу, из которого нужно загрузить данные.
     * @throws IllegalArgumentException Если произошла ошибка при чтении файла или данные некорректны.
     */
    override fun loadFromFile(filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                inputManager.startScriptRead(filePath)
                BufferedInputStream(FileInputStream(file)).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                        var lineNumber = 1
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            lineNumber++
                            if (lineNumber == 2) continue

                            val params = line!!.split(",").map { it.trim() }
                            if (params.size != 7) {
                                outputManager.println("Строка $lineNumber пропущена. Кол-во элементов ${params.size} вместо 7")
                                continue
                            }

                            try {
                                val name = params[0]
                                if (name.isBlank()) {
                                    throw IllegalArgumentException("Имя не может быть пустым")
                                }

                                val coordinateX = params[1].toLongOrNull()
                                    ?: throw NumberFormatException("coordinateX '${params[1]}' is not a Long")
                                if (coordinateX <= -818) {
                                    throw IllegalArgumentException("X должен быть > -818")
                                }

                                val coordinateY = params[2].toLongOrNull()
                                    ?: throw NumberFormatException("coordinateY '${params[2]}' is not a Long")
                                if (coordinateY > 730) {
                                    throw IllegalArgumentException("Y должен быть <= 730")
                                }

                                val enginePowerRaw = params[3].toFloatOrNull()
                                    ?: throw NumberFormatException("enginePower '${params[3]}' is not a Float")
                                if (enginePowerRaw <= 0) {
                                    throw IllegalArgumentException("Мощность должна быть > 0")
                                }
                                val enginePower = enginePowerRaw

                                val capacity = params[4].toFloatOrNull()
                                    ?: throw NumberFormatException("capacity '${params[4]}' is not a Float")
                                if (capacity <= 0) {
                                    throw IllegalArgumentException("Емкость должна быть > 0")
                                }

                                val distanceTravelled = params[5].toIntOrNull()
                                    ?: throw NumberFormatException("distanceTravelled '${params[5]}' is not an Int")
                                if (distanceTravelled < 0) {
                                    throw IllegalArgumentException("Пробег должен быть >= 0")
                                }

                                val fuelTypeString = params[6]
                                val fuelType = when (fuelTypeString.lowercase()) {
                                    "electricity", "электричество", "электро" -> FuelType.ELECTRICITY
                                    "antimatter", "анти", "антиматерия" -> FuelType.ANTIMATTER
                                    "diesel", "дизель", "диз" -> FuelType.DIESEL
                                    else -> throw IllegalArgumentException(
                                        "Некорректный тип топлива '$fuelTypeString'. " +
                                                "Возможные типы: ${FuelType.entries.joinToString(", ") { it.description }}"
                                    )
                                }

                                val vehicle = Vehicle(
                                    name = name,
                                    coordinates = Coordinates(coordinateX, coordinateY),
                                    enginePower = enginePower,
                                    capacity = capacity,
                                    distanceTravelled = distanceTravelled,
                                    fuelType = fuelType
                                )
                                collectionManager.getCollection().add(vehicle)
                            } catch (e: NumberFormatException) {
                                outputManager.println("Warning: Line $lineNumber skipped - ${e.message}")
                            } catch (e: IllegalArgumentException) {
                                outputManager.println("Warning: Line $lineNumber skipped - ${e.message}")
                            } catch (e: Exception) {
                                outputManager.println("Warning: Line $lineNumber skipped - invalid data: ${e.message}")
                            }
                        }
                    }
                }
                inputManager.finishScriptRead()
            } else {
                outputManager.println("File $filePath not found. Starting with empty collection.")
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Error reading CSV file: ${e.message}")
        }
    }

    /**
     * Сохраняет коллекцию транспортных средств в указанный CSV-файл.
     * Формат файла: name,coordinateX,coordinateY,enginePower,capacity,distanceTravelled,fuelType.
     *
     * @param filePath Путь к файлу, в который нужно сохранить данные.
     */
    override fun saveToFile() {
        try {
            val file = File(filePath)
            BufferedOutputStream(FileOutputStream(file)).use { outputStream ->
                val title = "name,coordinateX,coordinateY,enginePower,capacity,distanceTravelled,fuelType\n"
                outputStream.write(title.toByteArray())

                for (vehicle in collectionManager.getCollection()) {
                    val line = "${vehicle.name},${vehicle.coordinates.x},${vehicle.coordinates.y}," +
                            "${vehicle.enginePower},${vehicle.capacity},${vehicle.distanceTravelled}," +
                            "${vehicle.fuelType?.description ?: ""}\n"
                    outputStream.write(line.toByteArray(Charsets.UTF_8))
                }
            }
                outputManager.println("Коллекция успешно сохранена в файл: $filePath")
        } catch (e: Exception) {
            outputManager.println("Ошибка при сохранении. ${e.message}")
        }
    }
}