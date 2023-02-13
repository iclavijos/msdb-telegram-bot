package com.icesoft.msdb.telegram.bot.command

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import java.util.*

abstract class MSDBCommand(
    commandIdentifier: String,
    private val descriptionKey: String,
    private val longDescriptionKey: String,
    val restricted: Boolean = false) : BotCommand(commandIdentifier, "") {

    @Autowired
    protected lateinit var messageSource: MessageSource

    abstract fun getCommandIdentifierDescription(): String

    fun getDescription(userLanguageCode: String): String {
        return """
            <b>$COMMAND_INIT_CHARACTER${getCommandIdentifierDescription()}</b>
            ${messageSource.getMessage(descriptionKey, null, Locale.forLanguageTag(userLanguageCode))}
            """.trimIndent()
    }

    fun getExtendedDescription(userLanguageCode: String): String {
        return """
            <b>$COMMAND_INIT_CHARACTER${getCommandIdentifierDescription()}</b>
            ${messageSource.getMessage(longDescriptionKey, null, Locale.forLanguageTag(userLanguageCode))}
            """.trimIndent()
    }
}