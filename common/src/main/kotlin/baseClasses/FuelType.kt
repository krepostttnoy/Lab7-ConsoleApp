package baseClasses

import kotlinx.serialization.Serializable

/**
 * Перечисление, представляющее типы топлива для транспортных средств.
 * Каждое значение перечисления имеет описание, доступное через свойство [description].
 * Реализует интерфейс [Comparable], что позволяет сравнивать значения перечисления.
 *
 * @property description Описание типа топлива на русском языке.
 * @constructor Создаёт значение перечисления с указанным описанием.
 */
@Serializable
enum class FuelType(val description: String) : Comparable<FuelType> {
    /**
     * Электричество как тип топлива.
     */
    ELECTRICITY("Электричество"),

    /**
     * Дизельное топливо.
     */
    DIESEL("Дизель"),

    /**
     * Антиматерия как тип топлива.
     */
    ANTIMATTER("Антиматерия");
}