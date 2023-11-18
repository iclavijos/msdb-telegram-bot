package com.icesoft.msdb.telegram.bot.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import java.util.*

@Component
class HelpCommand:
    MSDBCommand("help", "help.help.basic", "help.help.extended", false, 1) {

    override fun getCommandIdentifierDescription(): String {
        val stringBuilder = StringBuilder()
        return stringBuilder.append(commandIdentifier).append(" [command]").toString()
    }

    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {
        if (ICommandRegistry::class.java.isInstance(absSender)) {
            val registry = absSender as ICommandRegistry
            if (arguments!!.isNotEmpty()) {
                val command = registry.getRegisteredCommand(arguments[0]) as MSDBCommand?
                if (command == null) {
                    absSender.execute(
                        SendMessage.builder().chatId(
                            chat!!.id
                        ).text(
                            messageSource.getMessage("help.invalidArgument", arrayOf(arguments[0]), Locale.forLanguageTag(user?.languageCode ?: "ES"))
                        ).parseMode("HTML").build()
                    )
                    return
                }
                val reply = command.getExtendedDescription(user?.languageCode ?: "ES")

                absSender.execute(
                    SendMessage.builder().chatId(
                        chat!!.id
                    ).text(reply).parseMode("HTML").build()
                )
            } else {
                val reply = StringBuilder()
                val commands = registry.registeredCommands as Collection<MSDBCommand>
                for (command in commands.sortedBy { it.order }) {
                    reply.append(command.getDescription(user?.languageCode ?: "ES")).append(System.lineSeparator()).append(System.lineSeparator())
                }

                absSender.execute(
                    SendMessage.builder().chatId(
                        chat!!.id
                    ).text(reply.toString()).parseMode("HTML").build()
                )
            }
        }
    }
}