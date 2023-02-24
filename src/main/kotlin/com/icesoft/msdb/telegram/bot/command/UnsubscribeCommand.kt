package com.icesoft.msdb.telegram.bot.command

import com.icesoft.msdb.telegram.bot.model.TelegramGroupSubscription
import com.icesoft.msdb.telegram.bot.service.SubscriptionsService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import java.util.*

@Component
class UnsubscribeCommand(val subscriptionsService: SubscriptionsService):
    MSDBCommand("unsubscribe", "help.unsubscribe.basic", "help.unsubscribe.extended", true) {

    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {
        val sendMessageRequest = SendMessage()

        sendMessageRequest.chatId = chat!!.id.toString()

        val subscribedSeries = subscriptionsService.getSubscriptions(chat.id)

        if (subscribedSeries.isEmpty()) {
            sendMessageRequest.text =
                messageSource.getMessage("unsubscribe.empty", null, Locale.forLanguageTag(user!!.languageCode))
        } else {
            sendMessageRequest.replyMarkup = generateSeriesInlineButtons(subscribedSeries, user)
            sendMessageRequest.text =
                messageSource.getMessage("unsubscribe.start", null, Locale.forLanguageTag(user!!.languageCode))
        }

        absSender!!.execute(sendMessageRequest)
    }

    fun handleCallbackQuery(absSender: AbsSender, callbackQuery: CallbackQuery) {
        val data = callbackQuery.data.split(":".toRegex()).toTypedArray()
        if (data[1] == "cancel") {
            endConversation(callbackQuery, absSender)
        } else if (data[1] == "confirm" || data[1] == "back") {

            if (data[1] == "confirm") subscriptionsService.unsubscribe(callbackQuery.message.chatId, data[2].toLong())

            val editMarkup = EditMessageText()
            editMarkup.chatId = callbackQuery.message.chatId.toString()
            editMarkup.inlineMessageId = callbackQuery.inlineMessageId
            editMarkup.text = messageSource.getMessage("unsubscribe.start", null, Locale.forLanguageTag(callbackQuery.from.languageCode))
            editMarkup.messageId = callbackQuery.message.messageId
            editMarkup.replyMarkup = generateSeriesInlineButtons(subscriptionsService.getSubscriptions(callbackQuery.message.chatId), callbackQuery.from)

            absSender.execute(editMarkup)
        } else {
            // We need to ask for confirmation
            val editMarkup = EditMessageText()
            editMarkup.chatId = callbackQuery.message.chatId.toString()
            editMarkup.inlineMessageId = callbackQuery.inlineMessageId
            editMarkup.text = messageSource.getMessage("unsubscribe.confirm", arrayOf(data[2]), Locale.forLanguageTag(callbackQuery.from.languageCode))
            editMarkup.enableMarkdown(true)
            editMarkup.messageId = callbackQuery.message.messageId

            val markupInline = InlineKeyboardMarkup()
            val rowsInline: MutableList<List<InlineKeyboardButton>> = mutableListOf()

            rowsInline.add(mutableListOf(
                InlineKeyboardButton.builder()
                    .text(messageSource.getMessage("unsubscribe.yes", null, Locale.forLanguageTag(callbackQuery.from.languageCode)))
                    .callbackData("unsubscribe:confirm:${data[1]}")
                    .build(),
                InlineKeyboardButton.builder()
                    .text(messageSource.getMessage("unsubscribe.no", null, Locale.forLanguageTag(callbackQuery.from.languageCode)))
                    .callbackData("unsubscribe:back")
                    .build()
            ))
            markupInline.keyboard = rowsInline
            editMarkup.replyMarkup = markupInline

            absSender.execute(editMarkup)
        }
    }

    private fun generateSeriesInlineButtons(
        subscribedSeries: List<TelegramGroupSubscription>,
        user: User?
    ): InlineKeyboardMarkup {
        val markupInline = InlineKeyboardMarkup()
        val rowsInline: MutableList<List<InlineKeyboardButton>> = mutableListOf()

        subscribedSeries.forEach { subscription ->
            rowsInline.add(
                mutableListOf(
                    InlineKeyboardButton.builder()
                        .text(subscription.seriesName)
                        .callbackData("unsubscribe:${subscription.id.seriesId}:${subscription.seriesName}")
                        .build()
                )
            )
        }
        rowsInline.add(
            mutableListOf(
                InlineKeyboardButton.builder()
                    .text(
                        messageSource.getMessage(
                            "cancel",
                            null,
                            Locale.forLanguageTag(user!!.languageCode)
                        )
                    )
                    .callbackData("unsubscribe:cancel")
                    .build()
            )
        )

        markupInline.keyboard = rowsInline
        return markupInline
    }

    private fun endConversation(
        callbackQuery: CallbackQuery,
        absSender: AbsSender
    ) {
        val deleteMessage = EditMessageReplyMarkup()
        deleteMessage.chatId = callbackQuery.message.chatId.toString()
        deleteMessage.messageId = callbackQuery.message.messageId
        deleteMessage.replyMarkup = InlineKeyboardMarkup()
        val rowsInline: MutableList<List<InlineKeyboardButton>> = ArrayList()
        deleteMessage.replyMarkup.keyboard = rowsInline

        absSender.execute(deleteMessage)
    }
}