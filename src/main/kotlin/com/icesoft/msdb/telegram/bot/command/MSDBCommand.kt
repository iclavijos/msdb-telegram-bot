package com.icesoft.msdb.telegram.bot.command

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import java.util.*

abstract class MSDBCommand(
    commandIdentifier: String,
    private val descriptionKey: String,
    private val longDescriptionKey: String,
    val restricted: Boolean = false) : BotCommand(commandIdentifier, "") {

    @Autowired
    protected lateinit var messageSource: MessageSource

    open fun getCommandIdentifierDescription(): String {
        return commandIdentifier
    }

    open fun endConversation(
        callbackQuery: CallbackQuery,
        absSender: AbsSender,
        endMessageText: String
    ) {
        val deleteMessage = EditMessageReplyMarkup()
        deleteMessage.chatId = callbackQuery.message.chatId.toString()
        deleteMessage.messageId = callbackQuery.message.messageId
        deleteMessage.replyMarkup = InlineKeyboardMarkup()
        val rowsInline: MutableList<List<InlineKeyboardButton>> = ArrayList()
        deleteMessage.replyMarkup.keyboard = rowsInline

        absSender.execute(deleteMessage)

        if (endMessageText != null) {
            val sendMessageRequest = SendMessage()

            sendMessageRequest.chatId = callbackQuery.message.chatId.toString()
            sendMessageRequest.enableHtml(true)
            sendMessageRequest.text = endMessageText
            absSender.execute(sendMessageRequest)
        }
    }

    fun getDescription(userLanguageCode: String): String {
        return """
            <b>$COMMAND_INIT_CHARACTER${getCommandIdentifierDescription()} ${if (restricted) "(" + messageSource.getMessage("admin", null, Locale.forLanguageTag(userLanguageCode)) + ")" else ""}</b>
            ${messageSource.getMessage(descriptionKey, null, Locale.forLanguageTag(userLanguageCode))}
            """.trimIndent()
    }

    fun getExtendedDescription(userLanguageCode: String): String {
        return """
            <b>$COMMAND_INIT_CHARACTER${getCommandIdentifierDescription()} ${if (restricted) "(" + messageSource.getMessage("admin", null, Locale.forLanguageTag(userLanguageCode)) + ")" else ""}</b>
            ${messageSource.getMessage(longDescriptionKey, null, Locale.forLanguageTag(userLanguageCode))}
            """.trimIndent()
    }
}