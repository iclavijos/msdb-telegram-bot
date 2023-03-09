package com.icesoft.msdb.telegram.bot.controllers

import com.icesoft.msdb.telegram.bot.command.*
import com.icesoft.msdb.telegram.bot.config.BotProperties
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
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
    private val subscribeCommand: SubscribeCommand,
    private val unsubscribeCommand: UnsubscribeCommand,
    private val settingsCommand: SettingsCommand,
    showCommand: ShowCommand,
    private val helpCommand: HelpCommand) : TelegramLongPollingCommandBot() {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    protected lateinit var messageSource: MessageSource

    private var commandPattern = "/(\\w+)@?\\w*".toRegex()

    init {
        register(subscribeCommand)
        register(unsubscribeCommand)
        register(settingsCommand)
        register(showCommand)

        register(helpCommand)
    }

    override fun getBotToken(): String {
        return botProperties.token
    }

    override fun getBotUsername(): String {
        return botProperties.username
    }

    override fun onUpdatesReceived(updates: MutableList<Update>?) {
        val filteredUpdates = updates?.filter { update ->
            val isReceiverAnotherBot = (update.message?.text?.contains("@") == true) && (update.message?.text?.contains(
                "@${botUsername}",
                true
            ) == false)
            update.hasCallbackQuery() || (update.message?.chat?.isGroupChat == false) || ((update.message?.chat?.isGroupChat == true) && !isReceiverAnotherBot)
        }
        super.onUpdatesReceived(filteredUpdates)
    }

    override fun filter(message: Message?): Boolean {
        val commandTxt = message!!.text.split("\\s+".toRegex())[0]

        var command = getRegisteredCommand(commandPattern.matchEntire(commandTxt)?.groupValues?.get(1)) as MSDBCommand?

        if (command == null && commandTxt == "/start") {
            message.text = "/help"
            command = helpCommand
        } else if (command == null) {
            logger.warn("Command not found: ${message.text}")
        }

        if (message.chat?.isUserChat!!) return false

        if (command?.restricted == true) {
            val chatMemberCommand = GetChatMember.builder()
                .userId(message.from!!.id)
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
            if (callbackData!!.startsWith("start")) {
                subscribeCommand.handleCallbackQuery(this, update.callbackQuery)
            } else if (callbackData.startsWith("unsubscribe")) {
                unsubscribeCommand.handleCallbackQuery(this, update.callbackQuery)
            } else if (callbackData.startsWith("settings")) {
                settingsCommand.handleCallbackQuery(this, update.callbackQuery)
            }
            return
        }

        if (update?.message?.isCommand == true) {
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
        if (!update?.message?.isGroupMessage!!) {
            val commandUnknownMessage = SendMessage()
            commandUnknownMessage.chatId = update?.message?.chatId.toString()
            commandUnknownMessage.text = messageSource.getMessage(
                "error.wtf",
                arrayOf(
                    update?.message?.from?.userName
                        ?: "${update?.message?.from?.firstName} ${update?.message?.from?.lastName}"
                ),
                Locale.forLanguageTag(update?.message?.from?.languageCode)
            )

            execute(commandUnknownMessage)
        }
    }

}