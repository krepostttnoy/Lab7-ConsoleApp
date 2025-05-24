package org.example.users

import org.apache.logging.log4j.LogManager
import org.example.dbConnect.DbManager
import org.example.token.JWTManager
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.experimental.and


class UserManager(private val dbManager: DbManager) {
    private val lock = Any()
    private val logger = LogManager.getLogger(UserManager::class.java)
    private val jwtManager = JWTManager()

    private fun hashing(stringToHash: String, salt: String): String {
        var hashedString = ""
        try {
            val md = MessageDigest.getInstance("SHA-512")
            md.update(salt.toByteArray())
            val bytes = md.digest(stringToHash.toByteArray())
            md.reset()
            val sb = StringBuilder()
            for (element in bytes) {
                sb.append(((element and 0xff.toByte()) + 0x100).toString(16).substring(1))
            }
            hashedString = sb.toString()
        } catch (e: Exception) {
            logger.error(e.message)
        }
        return hashedString
    }

    private fun createSalt(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        val sb = StringBuilder()
        for (element in bytes) {
            sb.append(((element and 0xff.toByte()) + 0x100).toString(16).substring(1))
        }
        return sb.toString()
    }

    fun login(username: String, password: String) : String {
        val salt = dbManager.getSalt(username)
        val hashedPassword = hashing(password, salt)
        val authorized = dbManager.loginUser(username, hashedPassword)
        return if (authorized) {
            logger.debug("User $username authorized")
            val token = jwtManager.createJWS("server", username)
            token
        } else {
            ""
        }
    }
    fun register(username: String, password: String): String {
        val salt = createSalt()
        val registered = dbManager.registerUser(username, hashing(password, salt), salt)
        return if (registered) {
            logger.debug("User $username was registered")
            val token = jwtManager.createJWS("server", username)
            token
        } else {
            logger.debug("Registration is nit finished, $username")
            ""
        }
    }

    fun userExists(username: String): Boolean {
        return dbManager.userExists(username)
    }

    fun updateUser(data: User, user: User) {
        synchronized(lock){
            user.setName(data.getName())
            user.setPassword(data.getPassword())
        }
    }

}