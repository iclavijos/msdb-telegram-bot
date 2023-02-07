package com.icesoft.msdb.telegram.bot.repository

import com.icesoft.msdb.telegram.bot.model.TelegramGroupSubscription
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionsRepository : MongoRepository<TelegramGroupSubscription, Long> {

    fun findByChatId(chatId: Long): List<TelegramGroupSubscription>
}