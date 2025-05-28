package org.example.dbConnect
import baseClasses.Coordinates
import baseClasses.FuelType
import baseClasses.Vehicle
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.serverUtils.ConnectionManager
import org.w3c.dom.xpath.XPathResult
import utils.JsonCreator
import utils.exceptions.UserNotFoundException
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper
import java.lang.Exception
import java.sql.Connection
import java.sql.Date
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import javax.swing.plaf.basic.BasicPasswordFieldUI
import kotlin.math.log

class DbManager(
    private val url: String,
    private val user: String,
    private val password: String
) {

    init {
        Class.forName("org.postgresql.Driver")
    }

    private val logger: Logger = LogManager.getLogger(DbManager::class.java)
    private val connectionManager = ConnectionManager()
    private val jsonCreator = JsonCreator()

    private fun getConnection(): Connection{
        return DriverManager.getConnection(url, user, password)
    }

    private fun initUsers(){
        getConnection().use {connection ->
            val statement = connection.createStatement()
            statement.executeUpdate("create table if not exists users(login varchar(50) primary key,password varchar(500),salt varchar(100));")
        }
    }

    fun initDB(){
        logger.debug("Inits DB")
        initUsers()
        initCollection()
    }

    fun registerUser(login: String, password: String, salt: String): Boolean{
        getConnection().use{connection ->
            val statement = connection.prepareStatement("select * from users where login = ?")
            statement.setString(1, login)
            statement.executeQuery().use {resultSet ->
                val resNext = resultSet.next()
                if(!resNext){
                    val adding = connection.prepareStatement("insert into users (login, password, salt) values (?, ?, ?)")
                    adding.setString(1, login)
                    adding.setString(2, password)
                    adding.setString(3, salt)
                    adding.executeUpdate()
                }
                return !resNext
            }
        }
    }

    fun getSalt(login: String): String{
        getConnection().use{connection ->
            val statement = connection.prepareStatement("select salt from users where login = ?")
            statement.setString(1, login)
            statement.executeQuery().use { resultSet ->
                return if (resultSet.next()){
                    resultSet.getString("salt")
                }else{
                    throw UserNotFoundException()
                }
            }
        }
    }

    fun userExists(login: String): Boolean{
        getConnection().use{connection ->
            val statement = connection.prepareStatement("select * from users where login = ?")
            statement.setString(1, login)
            statement.executeQuery().use{resultSet ->
                return resultSet.next()
            }
        }
    }

    fun loginUser(login: String, password: String): Boolean{
        getConnection().use{connection ->
            val statement = connection.prepareStatement("select * from users where login = ? and password = ?")
            statement.setString(1, login)
            statement.setString(2, password)
            statement.executeQuery().use{resultSet ->
                return resultSet.next()
            }
        }
    }


//    fun changePassword(login: String, oldPw: String, newPw: String){
//        getConnection().use{connection ->
//            val statement = connection.createStatement()
//            statement.executeQuery("select * from users where login = '$login' and password = '$oldPw'").use{resultSet ->
//                if (resultSet.next()){
//                    statement.executeUpdate("update users set password = '$newPw' where login = '$login'")
//                }
//            }
//        }
//    }

//    fun changeUsername(oldLog: String, newLog: String, password: String){
//        getConnection().use{connection ->
//            val statement = connection.createStatement()
//            statement.executeQuery("select * from users where login = '$oldLog' and password = '$password'").use{resultSet ->
//                if (resultSet.next()){
//                    statement.executeUpdate("update users set login = '$newLog' where login = '$oldLog'")
//                }
//            }
//        }
//    }

    fun getUsers(){
        getConnection().use{connection ->
            val statement = connection.createStatement()
            statement.executeQuery("select * from users").use{resultSet ->
                while (resultSet.next()){
                    val login = resultSet.getString("login")
                    val password = resultSet.getString("password")
                    println("Login: $login, password: $password")
                }
            }
        }
    }

    fun userOwned(vehicleId: Int, username: String): Boolean {
        getConnection().use { connection ->
            connection.prepareStatement(
                "SELECT 1 FROM collection WHERE id = ? AND user_login = ?"
            ).use { stmt ->
                stmt.setInt(1, vehicleId)
                stmt.setString(2, username)

                stmt.executeQuery().use { rs ->
                    return rs.next()
                }
            }
        }
    }

    fun deleteVehicles(ids: List<Int>) {
        if (ids.isEmpty()) return

        getConnection().use { connection ->
            connection.autoCommit = false
            try {
                val params = ids.joinToString(",") { "?" }

                connection.prepareStatement(
                    "DELETE FROM collection WHERE id IN ($params)"
                ).use { stmt ->
                    ids.forEachIndexed { index, id ->
                        stmt.setInt(index + 1, id)
                    }
                    stmt.executeUpdate()
                }
                connection.commit()
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }
    }

    fun updateVehicle(id: Int, newVehicle: Vehicle, username: String) {
        getConnection().use { connection ->
            connection.autoCommit = false
            try {
                // 1. Проверяем существование и принадлежность
                val existsAndOwned = connection.prepareStatement(
                    "SELECT 1 FROM collection WHERE id = ? AND user_login = ?"
                ).use { stmt ->
                    stmt.setInt(1, id)
                    stmt.setString(2, username)
                    stmt.executeQuery().use { rs -> rs.next() }
                }

                if (!existsAndOwned) {
                    throw SQLException("Vehicle not found or access denied")
                }

                // 2. Выполняем обновление
                connection.prepareStatement("""
                UPDATE collection 
                SET name = ?, coordinate_x = ?, coordinate_y = ?,
                    engine_power = ?, capacity = ?, distance_travelled = ?,
                    fuel_type = ?::fuelType
                WHERE id = ?
            """).use { stmt ->
                    stmt.setString(1, newVehicle.name)
                    stmt.setInt(2, newVehicle.coordinates.x.toInt())
                    stmt.setInt(3, newVehicle.coordinates.y.toInt())
                    stmt.setFloat(4, newVehicle.enginePower ?: throw SQLException("Engine power required"))
                    stmt.setFloat(5, newVehicle.capacity)
                    stmt.setInt(6, newVehicle.distanceTravelled)
                    stmt.setString(7, newVehicle.fuelType?.name ?: throw SQLException("Fuel type required"))
                    stmt.setInt(8, id)

                    val updated = stmt.executeUpdate()
                    if (updated != 1) {
                        throw SQLException("Update failed - vehicle not found")
                    }
                }
                connection.commit()
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }
    }
//    fun deleteUser(login: String, password: String) {
//        getConnection().use{connection ->
//            val statement = connection.createStatement()
//            statement.executeQuery("select * from users where login = '$login' and password = '$password'").use{resultSet ->
//                if (resultSet.next()){
//                    statement.executeUpdate("delete from users where login = '$login'")
//                }
//            }
//        }
//    }

    fun deleteVehicle(id: Int?){
        getConnection().use{connection ->
            if (id != null){
                val statement = connection.prepareStatement("delete from collection where id = ?")
                statement.setInt(1, id)
                statement.executeUpdate()
            }
        }
    }

    fun loadCollection(): List<Pair<Vehicle, String>> {
        getConnection().use { connection ->
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("""
            SELECT id, creation_date, name, coordinate_x, coordinate_y, 
                   engine_power, capacity, distance_travelled, fuel_type, user_login 
            FROM collection
        """)

            val result = mutableListOf<Pair<Vehicle, String>>()

            while (resultSet.next()) {
                try {
                    val vehicle = Vehicle(
                        name = resultSet.getString("name"),
                        coordinates = Coordinates(
                            resultSet.getInt("coordinate_x").toLong(),
                            resultSet.getInt("coordinate_y").toLong()
                        ),
                        enginePower = resultSet.getFloat("engine_power").takeIf { !resultSet.wasNull() },
                        capacity = resultSet.getFloat("capacity"),
                        distanceTravelled = resultSet.getInt("distance_travelled"),
                        fuelType = resultSet.getString("fuel_type")?.let { FuelType.valueOf(it) },
                        creationDate = Date(resultSet.getTimestamp("creation_date").time)
                    ).apply {
                        // Устанавливаем ID из базы данных и добавляем в existingIds
                        setId(resultSet.getInt("id"))
                    }

                    result.add(vehicle to resultSet.getString("user_login"))
                } catch (e: Exception) {
                    // Логируем ошибку, но продолжаем загрузку остальных элементов
                    System.err.println("Error loading vehicle: ${e.message}")
                }
            }

            resultSet.close()
            statement.close()
            return result
        }
    }

//    fun loadPasswordByUsername(login: String): String {
//        getConnection().use { connection ->
//            val statement = connection.createStatement()
//            statement.executeQuery("select * from users where login = '$login'").use { resultSet ->
//                resultSet.next()
//                return resultSet.getString("password")
//            }
//        }
//    }

    private fun initCollection() {
        getConnection().use { connection ->
            val statement = connection.createStatement()

            statement.execute("""
            DO $$ 
            BEGIN
                CREATE TYPE fueltype AS ENUM (
                    'antimatter', 
                    'electricity', 
                    'diesel'
                );
            EXCEPTION WHEN duplicate_object THEN 
                NULL;
            END $$;
        """.trimIndent())

            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS collection (\n" +
                        "    id SERIAL PRIMARY KEY,\n" +
                        "    creation_date TIMESTAMP NOT NULL,\n" +
                        "    name VARCHAR(100) NOT NULL,\n" +
                        "    coordinate_x int not null CHECK ( coordinate_x > -818 ),\n" +
                        "    coordinate_y int not null CHECK ( coordinate_y < 730 ),\n" +
                        "    engine_power float,\n" +
                        "    capacity float not null,\n" +
                        "    distance_travelled int not null,\n" +
                        "    fuel_type fueltype,\n" +
                        "    user_login VARCHAR(50) REFERENCES users(login) ON DELETE SET NULL ON UPDATE CASCADE\n" +
                        ");"
            )
        }
    }



    fun saveVehicle(vehicle: Vehicle, username: String) {
        getConnection().use { connection ->
            if (vehicle.getId() < 0) {
                // Вставка новой записи — id будет сгенерирован БД
                connection.prepareStatement("""
                INSERT INTO collection 
                (creation_date, name, coordinate_x, coordinate_y,
                 engine_power, capacity, distance_travelled, fuel_type, user_login)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?::fuelType, ?)
                RETURNING id
            """).use { stmt ->
                    stmt.setTimestamp(1, vehicle.getSqlCreationDate())
                    stmt.setString(2, vehicle.name)
                    stmt.setInt(3, vehicle.coordinates.x.toInt())
                    stmt.setInt(4, vehicle.coordinates.y.toInt())
                    stmt.setFloat(5, vehicle.enginePower!!)
                    stmt.setFloat(6, vehicle.capacity)
                    stmt.setInt(7, vehicle.distanceTravelled)
                    stmt.setString(8, vehicle.fuelType?.name)
                    stmt.setString(9, username)

                    val rs = stmt.executeQuery()
                    if (rs.next()) {
                        vehicle.setId(rs.getInt("id")) // Установить id, сгенерированный БД
                    }
                }
            } else {
                // Обновление существующей записи
                connection.prepareStatement("""
                UPDATE collection 
                SET creation_date = ?, name = ?, coordinate_x = ?, coordinate_y = ?,
                    engine_power = ?, capacity = ?, distance_travelled = ?,
                    fuel_type = ?::fuelType, user_login = ?
                WHERE id = ?
            """).use { stmt ->
                    stmt.setTimestamp(1, vehicle.getSqlCreationDate())
                    stmt.setString(2, vehicle.name)
                    stmt.setInt(3, vehicle.coordinates.x.toInt())
                    stmt.setInt(4, vehicle.coordinates.y.toInt())
                    stmt.setFloat(5, vehicle.enginePower!!)
                    stmt.setFloat(6, vehicle.capacity)
                    stmt.setInt(7, vehicle.distanceTravelled)
                    stmt.setString(8, vehicle.fuelType?.name)
                    stmt.setString(9, username)
                    stmt.setInt(10, vehicle.getId())
                    stmt.executeUpdate()
                }
            }
        }
    }
}