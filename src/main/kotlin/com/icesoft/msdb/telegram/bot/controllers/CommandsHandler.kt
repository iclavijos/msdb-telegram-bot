package com.icesoft.msdb.telegram.bot.controllers

import com.icesoft.msdb.telegram.bot.command.ShowCommand
import com.icesoft.msdb.telegram.bot.command.SubscribeCommand
import com.icesoft.msdb.telegram.bot.command.UnsubscribeCommand
import com.icesoft.msdb.telegram.bot.config.BotProperties
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.HelpCommand
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner
import java.util.*


@Slf4j
@Component
class CommandsHandler(
    private val botProperties: BotProperties,
    private val startCommand: SubscribeCommand,
    private val unsubscribeCommand: UnsubscribeCommand,
    showCommand: ShowCommand) : TelegramLongPollingCommandBot() {

    @Autowired
    protected lateinit var messageSource: MessageSource

    init {
        register(startCommand)
        register(unsubscribeCommand)
        register(showCommand)

        register(HelpCommand())
    }

    override fun getBotToken(): String {
        return botProperties.token
    }

    override fun getBotUsername(): String {
        return botProperties.username
    }

    override fun filter(message: Message?): Boolean {
        if (message?.chat?.isUserChat == true) return false

        if (message?.text?.startsWith("/subscribe", true)!! ||
            message.text?.startsWith("/unsubscribe", true)!!) {
            // This check should be better handled with inheritance. Will review that later on
            val chatMemberCommand = GetChatMember.builder()
                .userId(message.from.id)
                .chatId(message.chatId)
                .build()
            val chatMember = execute(chatMemberCommand)
            return chatMember !is ChatMemberAdministrator && chatMember !is ChatMemberOwner
        }
        return false
    }

    override fun processNonCommandUpdate(update: Update?) {
        if (update?.hasCallbackQuery() == true) {
            val callbackData = update.callbackQuery?.data
            if (callbackData!!.startsWith("start", false)) {
                startCommand.handleCallbackQuery(this, update.callbackQuery)
            } else if (callbackData.startsWith("unsubscribe", false)) {
                unsubscribeCommand.handleCallbackQuery(this, update.callbackQuery)
            }
            return
        }

        if (update?.message?.isCommand!!) {
            // We got here as the message was filtered due to lack of privileges of user
            val sendMessageRequest = SendMessage()

            sendMessageRequest.chatId = update.message?.chatId.toString()

            sendMessageRequest.text = messageSource.getMessage(
                "error.noPrivileges",
                arrayOf(update.message?.from?.userName ?: "${update.message?.from?.firstName} ${update.message?.from?.lastName}"),
                Locale.forLanguageTag(update.message?.from?.languageCode ?: "ES")
            )
            execute(sendMessageRequest)
        }
    }

    override fun processInvalidCommandUpdate(update: Update?) {
        val commandUnknownMessage = SendMessage()
        commandUnknownMessage.chatId = update?.message?.chatId.toString()
        commandUnknownMessage.text = messageSource.getMessage(
            "error.wtf",
            arrayOf(update?.message?.from?.userName),
            Locale.forLanguageTag(update?.message?.from?.languageCode))

        execute(commandUnknownMessage)
    }

}