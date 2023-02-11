package com.icesoft.msdb.telegram.bot.command

import com.icesoft.msdb.telegram.bot.model.TelegramGroupSubscription
import com.icesoft.msdb.telegram.bot.service.SubscriptionsService
import freemarker.template.Configuration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import java.io.StringWriter
import java.util.*


@Component
class ShowCommand(val subscriptionsService: SubscriptionsService,
                       commandIdentifier: String = "show",
                       description: String = "Show the series you are subscribed to") :
    BotCommand(commandIdentifier, description) {

    @Autowired
    protected lateinit var freeMarkerConfiguration: Configuration

    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {
        val subscriptions = subscriptionsService.getSubscriptions(chat!!.id)

        val model = mutableMapOf<String, List<TelegramGroupSubscription>>()
        model["subscriptions"] = subscriptions

        val template = freeMarkerConfiguration.getTemplate("subscriptions.ftlh", Locale.forLanguageTag(user!!.languageCode))
        val stringWriter = StringWriter()
        template.process(model, stringWriter)

        val sendMessageRequest = SendMessage()

        sendMessageRequest.chatId = chat.id.toString()
        sendMessageRequest.enableHtml(true)
        sendMessageRequest.enableWebPagePreview()
        sendMessageRequest.text = stringWriter.toString()
        absSender!!.execute(sendMessageRequest)

//        sendMessageRequest.text =
//            "[\u200B](${allSeries[0].logoUrl}) [${allSeries[0].name}](https://www.motorsports-database.racing/series/${allSeries[0].id}/view"
//        sendMessageRequest.enableMarkdown(true)
//        sendMessageRequest.replyMarkup = this.getGalleryView(0, -1, user!!.languageCode)
//
//        absSender!!.execute(sendMessageRequest)
    }
}