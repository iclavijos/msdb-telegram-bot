package com.icesoft.msdb.telegram.bot

import com.icesoft.msdb.telegram.bot.config.BotProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(BotProperties::class)
class MsdbTelegramBotApplication

fun main(args: Array<String>) {
	runApplication<MsdbTelegramBotApplication>(*args)
}
