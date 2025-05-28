package org.example.commands.consoleCommands

import kotlinx.serialization.Serializable
import utils.wrappers.RequestWrapper
import utils.wrappers.ResponseWrapper

@Serializable
sealed interface CommandTest {
    val interactive: Boolean
    fun execute(request: RequestWrapper, username: String): ResponseWrapper
    fun getInfo(): String
    fun getArgsType(): Map<String, String>
}