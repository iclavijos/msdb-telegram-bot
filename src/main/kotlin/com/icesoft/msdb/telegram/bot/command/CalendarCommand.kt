package com.icesoft.msdb.telegram.bot.command

import com.icesoft.msdb.telegram.bot.client.SeriesClient
import com.icesoft.msdb.telegram.bot.client.SeriesEditionClient
import com.icesoft.msdb.telegram.bot.model.EventEdition
import com.icesoft.msdb.telegram.bot.model.Series
import com.icesoft.msdb.telegram.bot.model.SeriesEdition
import freemarker.template.Configuration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import java.io.StringWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class CalendarCommand:
    MSDBCommand("calendar", "help.calendar.basic", "help.calendar.extended", false, 70) {

    @Autowired
    private lateinit var seriesClient: SeriesClient
    @Autowired
    private lateinit var seriesEditionClient: SeriesEditionClient
    @Autowired
    private lateinit var freeMarkerConfiguration: Configuration

    override fun getCommandIdentifierDescription(): String {
        val stringBuilder = StringBuilder()
        return stringBuilder.append(commandIdentifier).append(" [series name]").toString()
    }

    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {
        var sendMessageRequest = SendMessage()
        sendMessageRequest.chatId = chat!!.id.toString()
        sendMessageRequest.enableHtml(true)

        if (arguments.isNullOrEmpty()) {
            sendMessageRequest.text =
                messageSource.getMessage("calendar.noParams", null, Locale.forLanguageTag(user!!.languageCode ?: "ES"))
        } else {
            val searchTerm = arguments.joinToString(" ")
            val series = seriesClient.getSeries(searchTerm, Pageable.ofSize(10))
            if (series.isNotEmpty()) {
                if (series.count { item -> item.name.contains(searchTerm, true) || item.shortname.contains(searchTerm, true) } == 1) {
                    sendMessageRequest = handleSeriesReply(series.first().id, user!!, chat.id.toString())
                } else {
                    sendMessageRequest.text =
                        messageSource.getMessage("calendar.tooManyResults", null, Locale.forLanguageTag(user!!.languageCode ?: "ES"))
                    sendMessageRequest.enableMarkdown(true)
                    sendMessageRequest.replyMarkup = this.generateSeriesSelectorKeyboard(series)
                }
            } else {
                sendMessageRequest.text =
                    messageSource.getMessage("calendar.noResults", arrayOf(searchTerm), Locale.forLanguageTag(user!!.languageCode ?: "ES"))
            }
        }
        absSender?.execute(sendMessageRequest)
    }

    private fun generateSeriesSelectorKeyboard(series: List<Series>): InlineKeyboardMarkup {
        val numRows = series.size / 2 + series.size % 2
        val markupInline = InlineKeyboardMarkup()
        val rowsInline: MutableList<List<InlineKeyboardButton>> = mutableListOf()
        val inlineKeyboardButtonBuilder = InlineKeyboardButton.builder()

        for (rowCount in 1..numRows) {
            val rowInline: MutableList<InlineKeyboardButton> = mutableListOf()

            for (itemCounter in (rowCount - 1) * 2 until (rowCount) * 2) {
                if (itemCounter >= series.size) break
                val seriesItem = series[itemCounter]
                rowInline.add(inlineKeyboardButtonBuilder
                    .text(seriesItem.name)
                    .callbackData("calendar:series:${seriesItem.id}")
                    .build())
            }
            rowsInline.add(rowInline)
        }

        markupInline.keyboard = rowsInline

        return markupInline
    }

    private fun generateEditionsSelectorKeyboard(editions: List<SeriesEdition>): InlineKeyboardMarkup {
        val numRows = editions.size / 2 + editions.size % 2
        val markupInline = InlineKeyboardMarkup()
        val rowsInline: MutableList<List<InlineKeyboardButton>> = mutableListOf()
        val inlineKeyboardButtonBuilder = InlineKeyboardButton.builder()

        for (rowCount in 1..numRows) {
            val rowInline: MutableList<InlineKeyboardButton> = mutableListOf()

            for (itemCounter in (rowCount - 1) * 2 until (rowCount) * 2) {
                if (itemCounter >= editions.size) break
                val editionItem = editions[itemCounter]
                rowInline.add(inlineKeyboardButtonBuilder
                    .text(editionItem.period)
                    .callbackData("calendar:edition:${editionItem.id}")
                    .build())
            }

            rowsInline.add(rowInline)
        }

        markupInline.keyboard = rowsInline

        return markupInline
    }

    fun handleCallbackQuery(absSender: AbsSender, callbackQuery: CallbackQuery) {
        val data = callbackQuery.data.split(":".toRegex()).toTypedArray()
        if (data[1] == "series") {
            val sendMessageRequest = handleSeriesReply(data[2].toLong(), callbackQuery.from, callbackQuery.message.chat.id.toString())
            val deleteMessage = EditMessageReplyMarkup()
            deleteMessage.chatId = callbackQuery.message.chatId.toString()
            deleteMessage.messageId = callbackQuery.message.messageId
            deleteMessage.replyMarkup = InlineKeyboardMarkup()
            val rowsInline: MutableList<List<InlineKeyboardButton>> = ArrayList()
            deleteMessage.replyMarkup.keyboard = rowsInline

            absSender.execute(deleteMessage)
            absSender.execute(sendMessageRequest)
        } else {
            val seriesEdition = seriesEditionClient.getSeriesEdition(data[2].toLong())
            val events = seriesEditionClient.getSeriesEditionEvents(seriesEdition.id).map { it.eventEdition }
            endConversation(callbackQuery, absSender, getTextCalendar(seriesEdition, events, callbackQuery.from))
        }
    }

    private fun handleSeriesReply(seriesEditionId: Long, user: User, chatId: String): SendMessage {
        val sendMessageRequest = SendMessage()
        sendMessageRequest.chatId = chatId
        sendMessageRequest.enableHtml(true)

        val currentYear = LocalDate.now().year.toString()
        val pageable = PageRequest.of(0, 10, Sort.by("period").descending())
        var seriesEditions = seriesClient.getEditions(seriesEditionId, pageable)
        seriesEditions = seriesEditions.subList(0,
            seriesEditions.indexOfFirst { editionItem -> editionItem.period.contains(currentYear, ignoreCase = true) } + 1)
        if (seriesEditions.isEmpty()) {
            sendMessageRequest.text =
                messageSource.getMessage("calendar.noEditions", null, Locale.forLanguageTag(user.languageCode ?: "ES"))
        } else if (seriesEditions.size == 1) {
            val events = seriesEditionClient.getSeriesEditionEvents(seriesEditions[0].id).map { it.eventEdition }
            sendMessageRequest.text = getTextCalendar(seriesEditions[0], events, user)
        } else {
            sendMessageRequest.text =
                messageSource.getMessage("calendar.multipleEditions", null, Locale.forLanguageTag(user.languageCode ?: "ES"))
            sendMessageRequest.enableMarkdown(true)
            sendMessageRequest.replyMarkup = generateEditionsSelectorKeyboard(seriesEditions)
        }
        return sendMessageRequest
    }

    private fun getTextCalendar(series: SeriesEdition, events: List<EventEdition?>, user: User): String {
        val locale = Locale.forLanguageTag(user.languageCode ?: "ES")
        val sdf = if (locale.language == "EN") DateTimeFormatter.ofPattern("MM/dd/uuuu ", locale) else DateTimeFormatter.ofPattern("dd/MM/uuuu", locale)

        events.forEach { event -> event!!.formattedEventDate =  sdf.format(event.eventDate) }
        val model = mutableMapOf<String, Any>()
        model["editionName"] = series.editionName as Any
        model["events"] = events as Any

        val template = freeMarkerConfiguration.getTemplate("next_calendar.ftlh", locale)
        val stringWriter = StringWriter()
        template.process(model, stringWriter)

        return stringWriter.toString()
    }
}