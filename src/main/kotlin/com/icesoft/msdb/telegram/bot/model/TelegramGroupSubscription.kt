package com.icesoft.msdb.telegram.bot.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class TelegramGroupSubscription(
    val seriesId: Long,
    val chatId: Long
) {
    @Id
    var id: TelegramGroupSubscriptionKey = TelegramGroupSubscriptionKey(seriesId, chatId)
    var seriesName: String = ""
    var notifyRaces: Boolean = true
    var notifyQualifying: Boolean = false
    var notifyPractice: Boolean = false
}
