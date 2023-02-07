package com.icesoft.msdb.telegram.bot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("bot")
data class BotProperties(var username: String, val token: String)
