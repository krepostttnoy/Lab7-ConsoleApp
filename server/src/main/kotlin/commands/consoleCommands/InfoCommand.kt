package org.example.commands.consoleCommands

import collection.CollectionManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.commands.CommandInvoker
import org.example.serverUtils.ConnectionManager
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseType
import utils.wrappers.ResponseWrapper

/**
 * Команда для вывода информации о коллекции.
 * Использует [collection.CollectionManager] для получения и вывода информации о коллекции транспортных средств.
 *
 * @property cm Менеджер коллекции, содержащий информацию о коллекции.
 * @constructor Создаёт команду [InfoCommand] с заданным менеджером [cm].
 */
@Serializable
class InfoCommand(
    @Transient private val cm: CollectionManager = null!!,
    @Transient private val connectionManager: ConnectionManager = null!!
) : Command {
    override val interactive = false
    private val argsType = emptyMap<String, String>()
    private val info = "Returns info about collection"
    private val logger: Logger = LogManager.getLogger(Command::class.java)

    override fun getArgsType(): Map<String, String> {
        return argsType
    }

    override fun getInfo(): String {
        return info
    }
    /**
     * Выполняет команду вывода информации о коллекции.
     * Вызывает метод [CollectionManager.printCollectionInfo] для отображения информации.
     */
    override fun execute(request: RequestWrapper, username: String): ResponseWrapper {
        logger.info("execute info")
        logger.info("${request.token}: ${request.token}")
        val response = ResponseWrapper(ResponseType.OK, cm.printCollectionInfo(), receiver = username)
        logger.info("sexy shmara")
        return response
    }
}