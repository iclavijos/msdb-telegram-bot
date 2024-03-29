package com.icesoft.msdb.telegram.bot.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class TelegramGroupSettings(
    @Id
    var id: Long = 0,
    var languageCode: String = "ES",
    var minutesNotification: List<Int> = listOf(15, 60, 180)
)