package com.icesoft.msdb.telegram.bot.command

import com.icesoft.msdb.telegram.bot.model.TelegramGroupSettings
import com.icesoft.msdb.telegram.bot.repository.mongo.SettingsRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import java.util.*

@Component
class NotificationsCommand(private val settingsRepository: SettingsRepository):
    MSDBCommand("notifications", "help.notifications.basic", "help.notifications.extended", true, 50) {

    private val nonDigitsRegex = "(?!^\\d+\$)^.+\$".toRegex()

    override fun getCommandIdentifierDescription(): String {
        val stringBuilder = StringBuilder()
        return stringBuilder.append(commandIdentifier).append(" &lt;15|60|180&gt;").toString()
    }

    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {
        val sendMessageRequest = SendMessage()
        sendMessageRequest.chatId = chat!!.id.toString()

        if (arguments.isNullOrEmpty()) {
            sendMessageRequest.text =
                messageSource.getMessage("notifications.empty", null, Locale.forLanguageTag(user!!.languageCode ?: "ES"))
        } else if (arguments.any { arg ->  nonDigitsRegex.matches(arg)} || arguments.any { time -> !listOf(15, 60, 180).contains(time.toInt()) }) {
            sendMessageRequest.text =
                messageSource.getMessage("notifications.invalidArguments", null, Locale.forLanguageTag(user!!.languageCode ?: "ES"))
        } else {
            val settings = settingsRepository.findById(chat.id).orElse(TelegramGroupSettings())
            settings.id = chat.id
            settings.minutesNotification = arguments.map { arg -> arg.toInt() }.distinct()
            settingsRepository.save(settings)
            sendMessageRequest.text =
                messageSource.getMessage("notifications.success", null, Locale.forLanguageTag(user!!.languageCode ?: "ES"))
        }
        absSender!!.execute(sendMessageRequest)
    }
}