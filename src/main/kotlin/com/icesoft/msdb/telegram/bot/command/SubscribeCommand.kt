package com.icesoft.msdb.telegram.bot.command

import com.icesoft.msdb.telegram.bot.model.Series
import com.icesoft.msdb.telegram.bot.service.SubscriptionsService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import java.util.*

@Component
class SubscribeCommand(private val subscriptionsService: SubscriptionsService):
    MSDBCommand("subscribe", "help.subscribe.basic", "help.subscribe.extended", true) {

    private val BACK = "⬅️"
    private val NEXT = "➡️"
    private val INDEX_OUT_OF_RANGE = "Requested index is out of range!"

    private fun replaceLogoUrlExtension(series: Series): Series {
        series.logoUrl = series.logoUrl.replace(".png", ".jpg", true)
        return series
    }

    val allSeries: List<Series> = subscriptionsService.getSeries()
        .map { series -> replaceLogoUrlExtension(series) }
        .toList()

    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {
        val sendMessageRequest = SendMessage()

        sendMessageRequest.chatId = chat!!.id.toString()

        sendMessageRequest.text = messageSource.getMessage("start.welcome", null, Locale.forLanguageTag(user!!.languageCode ?: "ES"))
        absSender!!.execute(sendMessageRequest)

        sendMessageRequest.text =
            "[\u200B](${allSeries[0].logoUrl}) [${allSeries[0].name}](https://www.motorsports-database.racing/series/${allSeries[0].id}/view"
        sendMessageRequest.enableMarkdown(true)
        sendMessageRequest.replyMarkup = this.getGalleryView(0, -1, user.languageCode ?: "ES")

        absSender.execute(sendMessageRequest)
    }

    fun handleCallbackQuery(absSender: AbsSender, callbackQuery: CallbackQuery) {
        val data = callbackQuery.data.split(":".toRegex()).toTypedArray()
        if (data[1] == "gallery") {
            processGalleryCallback(data, absSender, callbackQuery)
        } else if (data[1] == "subscribe") {
            processSubscriptionCallback(data, absSender, callbackQuery)
        } else if (data[1] == "end") {
            endConversation(callbackQuery, absSender)
        }
    }

    private fun sendAnswerCallbackQuery(text: String, absSender: AbsSender, callbackQuery: CallbackQuery) {
        val answerCallbackQuery = AnswerCallbackQuery()
        answerCallbackQuery.callbackQueryId = callbackQuery.id
        answerCallbackQuery.showAlert = true
        answerCallbackQuery.text = text
        absSender.execute(answerCallbackQuery)
    }

    private fun getGalleryView(position: Int, action: Int, languageCode: String): InlineKeyboardMarkup? {
        /*
         * action = 1 -> back
         * action = 2 -> next
         * action = -1 -> nothing
         */
        var index = position
        if (action == 1 && index > 0) {
            index--
        } else if (action == 1 && index == 0) {
            return null
        } else if (action == 2 && index >= allSeries.size - 1) {
            return null
        } else if (action == 2) {
            index++
        }

        val markupInline = InlineKeyboardMarkup()
        val rowsInline: MutableList<List<InlineKeyboardButton>> = mutableListOf()
        val rowInline: MutableList<InlineKeyboardButton> = mutableListOf()

        val inlineKeyboardButtonBuilder = InlineKeyboardButton.builder()

        rowInline.add(inlineKeyboardButtonBuilder
            .text(messageSource.getMessage("start.subscribe", null, Locale.forLanguageTag(languageCode)))
            .callbackData("start:subscribe:$index:race")
            .build())

        val rowInline3: MutableList<InlineKeyboardButton> = mutableListOf()
        rowInline3.add(inlineKeyboardButtonBuilder
            .text(BACK)
            .callbackData("start:gallery:back:$index")
            .build())
        rowInline3.add(inlineKeyboardButtonBuilder
            .text(messageSource.getMessage("cancel", null, Locale.forLanguageTag(languageCode)))
            .callbackData("start:end")
            .build())
        rowInline3.add(inlineKeyboardButtonBuilder
            .text(NEXT)
            .callbackData("start:gallery:next:$index")
            .build())

        rowsInline.add(rowInline)
        rowsInline.add(rowInline3)
        markupInline.keyboard = rowsInline

        return markupInline
    }

    private fun processGalleryCallback(data: Array<String>, absSender: AbsSender, callbackQuery: CallbackQuery) {
        var index = data[3].toInt()
        var markup: InlineKeyboardMarkup? = null
        if (data[2] == "back") {
            markup = this.getGalleryView(data[3].toInt(), 1, callbackQuery.from.languageCode)
            if (index > 0) {
                index--
            }
        } else if (data[2] == "next") {
            markup = this.getGalleryView(data[3].toInt(), 2, callbackQuery.from.languageCode)
            if (index < allSeries.size - 1) {
                index++
            }
        }
        if (markup == null) {
            sendAnswerCallbackQuery(INDEX_OUT_OF_RANGE, absSender, callbackQuery)
        } else {
            val editMarkup = EditMessageText()
            editMarkup.chatId = callbackQuery.message.chatId.toString()
            editMarkup.inlineMessageId = callbackQuery.inlineMessageId
            editMarkup.text = "[\u200B](${allSeries[index].logoUrl}) [${allSeries[index].name}](https://www.motorsports-database.racing/series/${allSeries[index].id}/view"
            editMarkup.enableMarkdown(true)
            editMarkup.messageId = callbackQuery.message.messageId
            editMarkup.replyMarkup = markup

            absSender.execute(editMarkup)
        }
    }

    private fun processSubscriptionCallback(data: Array<String>, absSender: AbsSender, callbackQuery: CallbackQuery) {
        val index = data[2].toInt()
        val series = allSeries[index]
        val sessionType = data[3]
        val markupInline = InlineKeyboardMarkup()
        val rowsInline: MutableList<List<InlineKeyboardButton>> = mutableListOf()
        val rowInline: MutableList<InlineKeyboardButton> = mutableListOf()

        val inlineKeyboardButtonBuilder = InlineKeyboardButton.builder()

        val editMarkup = EditMessageText()
        editMarkup.chatId = callbackQuery.message.chatId.toString()
        editMarkup.inlineMessageId = callbackQuery.inlineMessageId
        editMarkup.enableMarkdown(true)

        when (sessionType) {
            "race" -> {
                editMarkup.text = messageSource.getMessage("start.qualifyings",
                    arrayOf(series.name),
                    Locale.forLanguageTag(callbackQuery.from.languageCode))

                rowInline.add(inlineKeyboardButtonBuilder
                    .text("YES")
                    .callbackData("start:subscribe:$index:qualifying")
                    .build())
                rowInline.add(inlineKeyboardButtonBuilder
                    .text("NO")
                    .callbackData("start:subscribe:$index:end:100")
                    .build())

                rowsInline.add(rowInline)

                markupInline.keyboard = rowsInline
            }
            "qualifying" -> {
                editMarkup.text = messageSource.getMessage("start.practices", null, Locale.forLanguageTag(callbackQuery.from.languageCode))
                rowInline.add(inlineKeyboardButtonBuilder
                    .text("YES")
                    .callbackData("start:subscribe:$index:practice:111")
                    .build())
                rowInline.add(inlineKeyboardButtonBuilder
                    .text("NO")
                    .callbackData("start:subscribe:$index:end:110")
                    .build())

                rowsInline.add(rowInline)

                markupInline.keyboard = rowsInline
            }
            "practice", "end" -> {
                val flags = data[4].toInt()
                subscriptionsService.subscribeChatToSeries(
                    callbackQuery.message.chatId,
                    allSeries[index].id,
                    true,
                    (flags - 100) / 10 == 1,
                    flags % 10 != 0
                )

                endConversation(callbackQuery, absSender)

                return
            }
        }
        messageSource.getMessage("start.end", null, Locale.forLanguageTag(callbackQuery.from.languageCode))
        editMarkup.messageId = callbackQuery.message.messageId
        editMarkup.replyMarkup = markupInline

        absSender.execute(editMarkup)
    }
}
