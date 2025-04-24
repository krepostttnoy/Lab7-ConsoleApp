package org.example.clientUtils.readers

import baseClasses.FuelType
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager

/**
 * Класс для чтения и валидации данных из консоли.
 * Реализует интерфейс [Read], предоставляя методы для чтения строк, целых чисел, чисел с плавающей точкой и длинных целых чисел.
 *
 * @constructor Создаёт экземпляр [Validator] для чтения данных из консоли.
 */
class Validator(
    private val outputManager: OutputManager,
    private val inputManager: InputManager
) : Read {

    /**
     * Читает строку из консоли и преобразует её в целое число.
     *
     * @return Целое число, если преобразование успешно, или null, если строка не является числом.
     */
    override fun readInt(): Int? {
        return readLineTrimmed().toIntOrNull()
    }

    /**
     * Читает строку из консоли и преобразует её в число с плавающей точкой.
     *
     * @return Число с плавающей точкой, если преобразование успешно, или null, если строка не является числом.
     */
    override fun readFloat(): Float? {
        return readLineTrimmed().toFloatOrNull()
    }

    /**
     * Читает строку из консоли и удаляет начальные и конечные пробелы.
     *
     * @return Обработанная строка или пустая строка, если ввод равен null.
     */
    override fun readLineTrimmed(): String {
        return inputManager.read().trim()
    }

    /**
     * Читает строку из консоли и преобразует её в длинное целое число.
     *
     * @return Длинное целое число.
     * @throws NumberFormatException Если строка не является числом.
     */
    override fun readLong(): Long {
        return readLineTrimmed().toLong()
    }

    fun validName(): String {
        return (if (inputManager.isScriptMode()) {
            val name = readLineTrimmed()
            if (name.isBlank()) throw IllegalArgumentException("Имя не может быть пустым.")
            name
        } else {
            while (true) {
                try {
                    outputManager.print("Введите имя: ")
                    val name = readLineTrimmed()
                    if (name.isNotBlank()) return name
                    outputManager.println("Ошибка: Имя не может быть пустым.")
                } catch (e: Exception) {
                    outputManager.println("Ошибка: ${e.message ?: "Неизвестная ошибка"}")
                }
            }
        }).toString()
    }

    fun validReadCoordinateX(): Long {
        return (if (inputManager.isScriptMode()) {
            val coordinate = readLong()
            if (coordinate <= -818) throw IllegalArgumentException("X должен быть > -818.")
            coordinate
        } else {
            while (true) {
                try {
                    outputManager.print("Введите координату X: ")
                    val coordinate = readLong()
                    if (coordinate > -818) return coordinate
                    outputManager.println("Ошибка: X должен быть > -818.")
                } catch (e: NumberFormatException) {
                    outputManager.println("Ошибка: Введено некорректное число. Попробуйте ещё раз.")
                } catch (e: IllegalArgumentException) {
                    outputManager.println("Ошибка: ${e.message}")
                }
            }
        }) as Long
    }

    fun validReadCoordinateY(): Long {
        return if (inputManager.isScriptMode()) {
            val coordinate = readLong()
            if (coordinate > 730) throw IllegalArgumentException("Y должен быть <= 730.")
            coordinate
        } else {
            while (true) {
                try {
                    outputManager.print("Введите координату Y: ")
                    val coordinate = readLong()
                    if (coordinate <= 730) return coordinate
                    outputManager.println("Ошибка: Y должен быть <= 730.")
                } catch (e: NumberFormatException) {
                    outputManager.println("Ошибка: Введено некорректное число. Попробуйте ещё раз.")
                } catch (e: IllegalArgumentException) {
                    outputManager.println("Ошибка: ${e.message}")
                }
            }
        } as Long
    }

    fun validReadEnginePower(): Float {
        return if (inputManager.isScriptMode()) {
            val enginePower = readFloat() ?: throw NumberFormatException("Мощность двигателя должна быть числом.")
            if (enginePower <= 0) throw IllegalArgumentException("Мощность должна быть > 0.")
            enginePower
        } else {
            while (true) {
                try {
                    outputManager.print("Введите мощность двигателя: ")
                    val enginePower = readFloat() ?: throw NumberFormatException("Введено некорректное число.")
                    if (enginePower > 0) return enginePower
                    outputManager.println("Ошибка: Мощность должна быть > 0.")
                } catch (e: NumberFormatException) {
                    outputManager.println("Ошибка: ${e.message}")
                } catch (e: IllegalArgumentException) {
                    outputManager.println("Ошибка: ${e.message}")
                }
            }
        } as Float
    }

    fun validReadCapacity(): Float {
        return if (inputManager.isScriptMode()) {
            val capacity = readFloat() ?: throw NumberFormatException("Емкость двигателя должна быть числом.")
            if (capacity <= 0) throw IllegalArgumentException("Емкость должна быть > 0.")
            capacity
        } else {
            while (true) {
                try {
                    outputManager.print("Введите емкость двигателя: ")
                    val capacity = readFloat() ?: throw NumberFormatException("Введено некорректное число.")
                    if (capacity > 0) return capacity
                    outputManager.println("Ошибка: Емкость должна быть > 0.")
                } catch (e: NumberFormatException) {
                    outputManager.println("Ошибка: ${e.message}")
                } catch (e: IllegalArgumentException) {
                    outputManager.println("Ошибка: ${e.message}")
                }
            }
        } as Float
    }

    fun validReadDistanceTravelled(): Int {
        return if (inputManager.isScriptMode()) {
            val distanceTravelled = readInt() ?: throw NumberFormatException("Пробег должен быть числом.")
            if (distanceTravelled < 0) throw IllegalArgumentException("Пробег должен быть >= 0.")
            distanceTravelled
        } else {
            while (true) {
                try {
                    outputManager.print("Введите пробег: ")
                    val distanceTravelled = readInt() ?: throw NumberFormatException("Введено некорректное число.")
                    if (distanceTravelled >= 0) return distanceTravelled
                    outputManager.println("Ошибка: Пробег должен быть >= 0.")
                } catch (e: NumberFormatException) {
                    outputManager.println("Ошибка: ${e.message}")
                } catch (e: IllegalArgumentException) {
                    outputManager.println("Ошибка: ${e.message}")
                }
            }
        } as Int
    }

    fun validReadFuelType(): FuelType {
        return (if (inputManager.isScriptMode()) {
            val fuelType = readLineTrimmed().lowercase()
            when (fuelType) {
                "электричество", "электро" -> FuelType.ELECTRICITY
                "анти", "антиматерия" -> FuelType.ANTIMATTER
                "дизель", "диз" -> FuelType.DIESEL
                else -> throw IllegalArgumentException(
                    "Некорректный тип топлива: $fuelType. Возможные типы: ${FuelType.entries.joinToString(", ") { it.description }}"
                )
            }
        } else {
            while (true) {
                try {
                    outputManager.print("Введите тип топлива: ")
                    val fuelType = readLineTrimmed().lowercase()
                    return when (fuelType) {
                        "электричество", "электро" -> FuelType.ELECTRICITY
                        "анти", "антиматерия" -> FuelType.ANTIMATTER
                        "дизель", "диз" -> FuelType.DIESEL
                        else -> throw IllegalArgumentException(
                            "Некорректный тип топлива! Возможные типы: ${FuelType.entries.joinToString(", ") { it.description }}"
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    outputManager.println("Ошибка: ${e.message}")
                }
            }
        }) as FuelType
    }
}