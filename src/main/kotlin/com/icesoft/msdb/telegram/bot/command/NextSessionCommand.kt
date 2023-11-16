package com.icesoft.msdb.telegram.bot.command

import com.icesoft.msdb.telegram.bot.client.EventEditionClient
import com.icesoft.msdb.telegram.bot.client.SeriesClient
import com.icesoft.msdb.telegram.bot.model.Series
import freemarker.template.Configuration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import java.io.StringWriter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class NextSessionCommand:
    MSDBCommand("nextSession", "help.nextSession.basic", "help.nextSession.extended", false) {

    @Autowired
    private lateinit var seriesClient: SeriesClient
    @Autowired
    private lateinit var eventEditionClient: EventEditionClient
    @Autowired
    private lateinit var freeMarkerConfiguration: Configuration

    override fun getCommandIdentifierDescription(): String {
        val stringBuilder = StringBuilder()
        return stringBuilder.append(commandIdentifier).append(" [series name]").toString()
    }

    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {
        val sendMessageRequest = SendMessage()
        sendMessageRequest.chatId = chat!!.id.toString()
        sendMessageRequest.enableHtml(true)
        sendMessageRequest.enableWebPagePreview()

        if (arguments.isNullOrEmpty()) {
            sendMessageRequest.text =
                messageSource.getMessage("nextSession.noParams", null, Locale.forLanguageTag(user!!.languageCode ?: "ES"))
        } else {
            val searchTerm = arguments.joinToString(" ")
            val series = seriesClient.getSeries(searchTerm, Pageable.ofSize(10))
            if (series.isNotEmpty()) {
                if (series.count { item -> item.name.contains(searchTerm, true) || item.shortname.contains(searchTerm, true) } == 1) {
                    sendMessageRequest.text = getTextNextSession(series.first(), user!!)
                } else {
                    sendMessageRequest.text =
                        messageSource.getMessage("nextSession.tooManyResults", null, Locale.forLanguageTag(user!!.languageCode ?: "ES"))
                    sendMessageRequest.enableMarkdown(true)
                    sendMessageRequest.replyMarkup = this.getGalleryView(series)
                }
            } else {
                sendMessageRequest.text =
                    messageSource.getMessage("nextSession.noResults", arrayOf(searchTerm), Locale.forLanguageTag(user!!.languageCode ?: "ES"))
            }
        }
        absSender?.execute(sendMessageRequest)
    }

    private fun getGalleryView(series: List<Series>): InlineKeyboardMarkup {
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
                    .callbackData("nextsession:${seriesItem.id}")
                    .build())
            }

            rowsInline.add(rowInline)
        }

        markupInline.keyboard = rowsInline

        return markupInline
    }

    fun handleCallbackQuery(absSender: AbsSender, callbackQuery: CallbackQuery) {
        val data = callbackQuery.data.split(":".toRegex()).toTypedArray()
        val series = seriesClient.getSeries(data[1].toLong())

        endConversation(callbackQuery, absSender, getTextNextSession(series, callbackQuery.from))

    }

    private fun getTextNextSession(series: Series, user: User): String {
        val eventSession = eventEditionClient.getNextSession(series.id)
        if (eventSession != null) {
            val locale = Locale.forLanguageTag(user.languageCode ?: "ES")
            val sdf = if (locale.language == "EN") DateTimeFormatter.ofPattern("EEEE, MMMM d uuuu - h:mm", locale) else DateTimeFormatter.ofPattern("EEEE, d MMMM uuuu - HH:mm", locale)
            val timeZone = if (eventSession.eventEdition.event.raid || eventSession.eventEdition.event.rally)
                eventSession.eventEdition.locationTimeZone
            else
                eventSession.eventEdition.trackLayout!!.racetrack.timeZone

            val model = mutableMapOf<String, Any>()
            model["seriesName"] = series.name as Any
            model["session"] = eventSession as Any
            model["startTimeLocal"] = sdf.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(eventSession.sessionStartTime), ZoneId.of(timeZone))) as Any
            model["startTimeMadrid"] = sdf.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(eventSession.sessionStartTime), ZoneId.of("Europe/Madrid"))) as Any
            model["startTimeNY"] = sdf.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(eventSession.sessionStartTime), ZoneId.of("America/New_York"))) as Any
            model["startTimeBBAA"] = sdf.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(eventSession.sessionStartTime), ZoneId.of("America/Argentina/Buenos_Aires"))) as Any
            model["startTimeSydney"] = sdf.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(eventSession.sessionStartTime), ZoneId.of("Australia/Sydney"))) as Any

            val template = freeMarkerConfiguration.getTemplate("next_session.ftlh", locale)
            val stringWriter = StringWriter()
            template.process(model, stringWriter)

            return stringWriter.toString()
        }

        return messageSource.getMessage("nextSession.noSession", null, Locale.forLanguageTag(user.languageCode ?: "ES"))
    }
}