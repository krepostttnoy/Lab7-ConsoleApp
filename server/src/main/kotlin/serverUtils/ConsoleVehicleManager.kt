package console

import baseClasses.Coordinates
import baseClasses.Vehicle
import org.example.serverUtils.IReadManager

/**
 * Класс для создания объекта [Vehicle] с данными, введёнными пользователем через консоль.
 * Реализует интерфейс [IVehicleManager], используя [IReadManager] для чтения данных.
 *
 * @property rm Менеджер для чтения данных из консоли.
 * @constructor Создаёт экземпляр [ConsoleVehicleManager] с заданным менеджером [rm].
 */
class ConsoleVehicleManager(private val rm: IReadManager) : IVehicleManager {

    /**
     * Создаёт новый объект [Vehicle] на основе данных, введённых пользователем.
     * Запрашивает все необходимые поля через [rm] и создаёт объект [Coordinates] для координат.
     *
     * @return Новый объект [Vehicle] с введёнными данными.
     */
    override fun setVehicle(): Vehicle {
        val name = rm.readName()
        val coordinateX = rm.readCoordinateX()
        val coordinateY = rm.readCoordinateY()
        val enginePower = rm.readEnginePower()
        val capacity = rm.readCapacity()
        val distanceTravelled = rm.readDistanceTravelled()
        val fuelType = rm.readFuelType()

        val coordinates = Coordinates(coordinateX, coordinateY)

        return Vehicle(name, coordinates, enginePower, capacity, distanceTravelled, fuelType)
    }
}