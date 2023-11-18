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
class LanguageCommand(private val settingsRepository: SettingsRepository) : MSDBCommand(
    "language", "help.language.basic", "help.language.extended", true, 60
) {
    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {
        val sendMessageRequest = SendMessage()
        val languageCode = user!!.languageCode ?: "ES"
        sendMessageRequest.chatId = chat!!.id.toString()

        sendMessageRequest.text = messageSource.getMessage("language.welcome", null, Locale.forLanguageTag(languageCode))

        sendMessageRequest.enableMarkdown(true)

        val markupInline = InlineKeyboardMarkup()
        val rowsInline: MutableList<List<InlineKeyboardButton>> = mutableListOf()
        val rowInline: MutableList<InlineKeyboardButton> = mutableListOf()

        val inlineKeyboardButtonBuilder = InlineKeyboardButton.builder()

        rowInline.add(inlineKeyboardButtonBuilder
            .text(messageSource.getMessage("language.english", null, Locale.forLanguageTag(languageCode)))
            .callbackData("language:confirm:EN")
            .build())
        rowInline.add(inlineKeyboardButtonBuilder
            .text(messageSource.getMessage("language.spanish", null, Locale.forLanguageTag(languageCode)))
            .callbackData("language:confirm:ES")
            .build())
        rowInline.add(inlineKeyboardButtonBuilder
            .text(messageSource.getMessage("language.catalan", null, Locale.forLanguageTag(languageCode)))
            .callbackData("language:confirm:CA")
            .build())
        rowInline.add(inlineKeyboardButtonBuilder
            .text(messageSource.getMessage("language.galician", null, Locale.forLanguageTag(languageCode)))
            .callbackData("language:confirm:GL")
            .build())
        rowInline.add(inlineKeyboardButtonBuilder
            .text(messageSource.getMessage("cancel", null, Locale.forLanguageTag(languageCode)))
            .callbackData("language:end")
            .build())

        rowsInline.add(rowInline)
        markupInline.keyboard = rowsInline

        sendMessageRequest.replyMarkup = markupInline

        absSender!!.execute(sendMessageRequest)
    }

    fun handleCallbackQuery(absSender: AbsSender, callbackQuery: CallbackQuery) {
        val data = callbackQuery.data.split(":".toRegex()).toTypedArray()
        var replyBack: String? = null
        if (data[1] == "confirm") {
            val languageCode = data[2]
            val settings = settingsRepository.findById(callbackQuery.message.chatId).orElse(TelegramGroupSettings(callbackQuery.message.chatId, languageCode))
            settings.languageCode = languageCode
            settingsRepository.save(settings)
            replyBack = messageSource.getMessage("start.end", null, Locale.forLanguageTag(callbackQuery.from.languageCode))
        }

        endConversation(callbackQuery, absSender, replyBack!!)
    }
}