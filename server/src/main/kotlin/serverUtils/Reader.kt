package org.example.serverUtils

import baseClasses.FuelType
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager

/**
 * Класс для чтения данных с консоли с валидацией.
 * Реализует интерфейс [IReadManager], предоставляя методы для ввода различных полей с проверкой корректности.
 *
 * @property console Объект для чтения и валидации данных из консоли.
 * @constructor Создаёт экземпляр [Reader] с объектом [Validator] для чтения данных.
 */
class Reader(
    private val outputManager: OutputManager,
    private val inputManager: InputManager
) : IReadManager {

    /**
     * Объект для чтения и валидации данных из консоли.
     */
    private val console = Validator(outputManager, inputManager)

    /**
     * Запрашивает у пользователя ввод имени транспортного средства.
     * Продолжает запрашивать ввод, пока не будет введено непустое имя.
     *
     * @return Введённое имя транспортного средства.
     */
    override fun readName(): String {
        return console.validName()
    }

    /**
     * Запрашивает у пользователя ввод координаты X.
     * Проверяет, что введённое значение больше -818.
     *
     * @return Введённая координата X.
     * @throws NumberFormatException Если введённое значение не является числом.
     * @throws IllegalArgumentException Если введённое значение меньше или равно -818.
     */
    override fun readCoordinateX(): Long {
        return console.validReadCoordinateX()
    }

    /**
     * Запрашивает у пользователя ввод координаты Y.
     * Проверяет, что введённое значение меньше или равно 730.
     *
     * @return Введённая координата Y.
     * @throws NumberFormatException Если введённое значение не является числом.
     * @throws IllegalArgumentException Если введённое значение больше 730.
     */
    override fun readCoordinateY(): Long {
        return console.validReadCoordinateY()
    }

    /**
     * Запрашивает у пользователя ввод мощности двигателя.
     * Проверяет, что введённое значение больше 0.
     *
     * @return Введённая мощность двигателя (может быть null).
     * @throws NumberFormatException Если введённое значение не является числом.
     * @throws IllegalArgumentException Если введённое значение меньше или равно 0.
     */
    override fun readEnginePower(): Float? {
        return console.validReadEnginePower()
    }

    /**
     * Запрашивает у пользователя ввод вместимости.
     * Проверяет, что введённое значение больше 0.
     *
     * @return Введённая вместимость.
     * @throws NumberFormatException Если введённое значение не является числом.
     * @throws IllegalArgumentException Если введённое значение меньше или равно 0.
     */
    override fun readCapacity(): Float {
        return console.validReadCapacity()
    }

    /**
     * Запрашивает у пользователя ввод пробега.
     * Проверяет, что введённое значение больше или равно 0.
     *
     * @return Введённый пробег.
     * @throws NumberFormatException Если введённое значение не является числом.
     * @throws IllegalArgumentException Если введённое значение меньше 0.
     */
    override fun readDistanceTravelled(): Int {
        return console.validReadDistanceTravelled()
    }

    /**
     * Запрашивает у пользователя ввод типа топлива.
     * Проверяет, что введённое значение соответствует одному из значений [FuelType].
     *
     * @return Введённый тип топлива в виде значения [FuelType].
     * @throws IllegalArgumentException Если введённый тип топлива некорректен.
     */
    override fun readFuelType(): FuelType {
        return console.validReadFuelType()
    }
}