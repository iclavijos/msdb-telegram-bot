package com.icesoft.msdb.telegram.bot.command

import com.icesoft.msdb.telegram.bot.model.TelegramGroupSubscription
import com.icesoft.msdb.telegram.bot.service.SubscriptionsService
import freemarker.template.Configuration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import java.io.StringWriter
import java.util.*


@Component
class ShowCommand(val subscriptionsService: SubscriptionsService) :
    MSDBCommand("show", "help.show.basic", "help.show.extended", false, 40) {

    @Autowired
    protected lateinit var freeMarkerConfiguration: Configuration

    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {
        val subscriptions = subscriptionsService.getSubscriptions(chat!!.id)

        if (subscriptions.isEmpty()) {
            val sendMessageRequest = SendMessage()

            sendMessageRequest.chatId = chat.id.toString()
            sendMessageRequest.text =
                messageSource.getMessage("show.empty", null, Locale.forLanguageTag(user!!.languageCode ?: "ES"))

            absSender!!.execute(sendMessageRequest)
            return
        }

        val model = mutableMapOf<String, List<TelegramGroupSubscription>>()
        model["subscriptions"] = subscriptions

        val template = freeMarkerConfiguration.getTemplate("subscriptions.ftlh", Locale.forLanguageTag(user!!.languageCode ?: "ES"))
        val stringWriter = StringWriter()
        template.process(model, stringWriter)

        val sendMessageRequest = SendMessage()

        sendMessageRequest.chatId = chat.id.toString()
        sendMessageRequest.enableHtml(true)
        sendMessageRequest.enableWebPagePreview()
        sendMessageRequest.text = stringWriter.toString()
        absSender!!.execute(sendMessageRequest)
    }

}