package com.icesoft.msdb.telegram.bot.command

import com.icesoft.msdb.telegram.bot.model.TelegramGroupSettings
import com.icesoft.msdb.telegram.bot.repository.mongo.SettingsRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import java.util.*

@Component
class SettingsCommand(private val settingsRepository: SettingsRepository) : MSDBCommand(
    "settings", "help.settings.basic", "help.settings.extended", true
) {
    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {
        val sendMessageRequest = SendMessage()
        val languageCode = user!!.languageCode ?: "ES"
        sendMessageRequest.chatId = chat!!.id.toString()

        sendMessageRequest.text = messageSource.getMessage("settings.welcome", null, Locale.forLanguageTag(languageCode))

        sendMessageRequest.enableMarkdown(true)

        val markupInline = InlineKeyboardMarkup()
        val rowsInline: MutableList<List<InlineKeyboardButton>> = mutableListOf()
        val rowInline: MutableList<InlineKeyboardButton> = mutableListOf()

        val inlineKeyboardButtonBuilder = InlineKeyboardButton.builder()

        rowInline.add(inlineKeyboardButtonBuilder
            .text(messageSource.getMessage("settings.english", null, Locale.forLanguageTag(languageCode)))
            .callbackData("settings:confirm:EN")
            .build())
        rowInline.add(inlineKeyboardButtonBuilder
            .text(messageSource.getMessage("settings.spanish", null, Locale.forLanguageTag(languageCode)))
            .callbackData("settings:confirm:ES")
            .build())
        rowInline.add(inlineKeyboardButtonBuilder
            .text(messageSource.getMessage("settings.catalan", null, Locale.forLanguageTag(languageCode)))
            .callbackData("settings:confirm:CA")
            .build())
        rowInline.add(inlineKeyboardButtonBuilder
            .text(messageSource.getMessage("cancel", null, Locale.forLanguageTag(languageCode)))
            .callbackData("settings:end")
            .build())

        rowsInline.add(rowInline)
        markupInline.keyboard = rowsInline

        sendMessageRequest.replyMarkup = markupInline

        absSender!!.execute(sendMessageRequest)
    }

    fun handleCallbackQuery(absSender: AbsSender, callbackQuery: CallbackQuery) {
        val data = callbackQuery.data.split(":".toRegex()).toTypedArray()
        var replyBack = false
        if (data[1] == "confirm") {
            val languageCode = data[2]
            settingsRepository.save(TelegramGroupSettings(callbackQuery.message.chatId, languageCode))
            replyBack = true
        }

        endConversation(callbackQuery, absSender, replyBack)
    }
}