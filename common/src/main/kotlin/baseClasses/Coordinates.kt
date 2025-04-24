package baseClasses

import kotlinx.serialization.Serializable

/**
 * Класс, представляющий координаты в двумерном пространстве.
 * Содержит два свойства: x и y, которые представляют координаты по осям.
 *
 * @property x Координата по оси X. Должна быть больше -818 и не равна null.
 * @property y Координата по оси Y. Должна быть меньше или равна 730.
 * @constructor Создаёт объект [Coordinates] с заданными координатами [x] и [y].
 * @throws IllegalArgumentException Если [x] меньше или равна -818 или [y] больше 730.
 */
@Serializable
data class Coordinates(
    val x: Long,
    val y: Long
) {
    init {
        require(x != null && x > -818) { "X - не должен быть null и меньше -818" }
        require(y <= 730) { "Y - не должен быть больше 730" }
    }

    /**
     * Возвращает строковое представление координат в формате "x, y".
     *
     * @return Строка, представляющая координаты, например "10, 20".
     */
    override fun toString(): String {
        return "$x, $y"
    }
}