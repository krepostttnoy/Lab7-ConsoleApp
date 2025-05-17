package org.example.dbConnect
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
import java.sql.DriverManager
import java.sql.ResultSet
import javax.swing.plaf.basic.BasicPasswordFieldUI

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
    private val connection = DriverManager.getConnection(url, user, password)

    private fun getConnection(): Connection{
        return DriverManager.getConnection(url, user, password)
    }

    private fun initUsers(){
        getConnection().use {connection ->
            val statement = connection.createStatement()
            statement.executeUpdate("create table if not exists users(login varchar(50) primary key,password varchar(500),salt varchar(100));")
        }
    }

    private fun initCollection(){
        getConnection().use {connection ->
            val statement = connection.createStatement()
            statement.executeUpdate("create table if not exists collection(id serial primary key,info varchar(1000) not null, user_login varchar(50) references users (login) ON DELETE SET NULL ON UPDATE CASCADE);")
        }
    }

    fun initDB(){
        println("goida")
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

    fun changePassword(login: String, oldPw: String, newPw: String){
        getConnection().use{connection ->
            val statement = connection.createStatement()
            statement.executeQuery("select * from users where login = '$login' and password = '$oldPw'").use{resultSet ->
                if (resultSet.next()){
                    statement.executeUpdate("update users set password = '$newPw' where login = '$login'")
                }
            }
        }
    }

    fun changeUsername(oldLog: String, newLog: String, password: String){
        getConnection().use{connection ->
            val statement = connection.createStatement()
            statement.executeQuery("select * from users where login = '$oldLog' and password = '$password'").use{resultSet ->
                if (resultSet.next()){
                    statement.executeUpdate("update users set login = '$newLog' where login = '$oldLog'")
                }
            }
        }
    }

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

    fun deleteUser(login: String, password: String) {
        getConnection().use{connection ->
            val statement = connection.createStatement()
            statement.executeQuery("select * from users where login = '$login' and password = '$password'").use{resultSet ->
                if (resultSet.next()){
                    statement.executeUpdate("delete from users where login = '$login'")
                }
            }
        }
    }

    fun deleteVehicle(id: Int){
        getConnection().use{connection ->
            val statement = connection.prepareStatement("delete from collection where id = ?")
            statement.setInt(1, id)
            statement.executeUpdate()
        }
    }

    fun loadCollection(): Map<String, String>{
        getConnection().use{connection ->
            val statement = connection.createStatement()
            statement.executeQuery("select * from collection").use{resultSet ->
                val result = mutableMapOf<String, String>()
                while (resultSet.next()){
                    result[resultSet.getString("info")] = resultSet.getString("user_login")
                }
                return result
            }
        }
    }

    fun loadPasswordByUsername(login: String): String{
        getConnection().use{connection ->
            val statement = connection.createStatement()
            statement.executeQuery("select * from users where login = '$login'").use{resultSet ->
                resultSet.next()
                return resultSet.getString("password")
            }
        }
    }

    fun getIdByVehicle(vehicle: Vehicle): Int{
        getConnection().use{connection ->
            val statement = connection.createStatement()
            statement.executeQuery("select * from collection where info = '${jsonCreator.objectToString(vehicle)}'").use{resultSet ->
                resultSet.next()
                return resultSet.getInt("id")
            }
        }
    }

    fun saveVehicle(vehicle: Vehicle, username: String){
        getConnection().use{connection ->
            var statement = connection.prepareStatement("select * from collection where id = ?")
            val id = vehicle.getId()
            statement.setInt(1, id)
            statement.executeQuery().use{resultSet ->
                val result = resultSet.next()

                if(result){
                    statement = connection.prepareStatement("update collection set info = ? where id = ?")
                    statement.setString(1, jsonCreator.objectToString(vehicle))
                    statement.setInt(2, vehicle.getId())
                    statement.executeUpdate()
                }else{
                    statement = connection.prepareStatement("insert into collection (info, user_login) values (?, ?)")
                    statement.setString(1, jsonCreator.objectToString(vehicle))
                    statement.setString(2, username)
                    statement.executeUpdate()
                }
            }
        }
    }

}