package com.icesoft.msdb.telegram.bot

import com.icesoft.msdb.telegram.bot.config.BotProperties
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignAutoConfiguration

@SpringBootApplication
@EnableConfigurationProperties(BotProperties::class)
@EnableFeignClients
@ImportAutoConfiguration(FeignAutoConfiguration::class)
class MsdbTelegramBotApplication

fun main(args: Array<String>) {
	runApplication<MsdbTelegramBotApplication>(*args)
}
