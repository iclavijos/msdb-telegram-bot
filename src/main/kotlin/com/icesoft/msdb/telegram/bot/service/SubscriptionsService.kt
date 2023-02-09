package com.icesoft.msdb.telegram.bot.service

import com.icesoft.msdb.telegram.bot.model.Series
import com.icesoft.msdb.telegram.bot.model.TelegramGroupSubscription
import com.icesoft.msdb.telegram.bot.repository.jpa.SeriesRepository
import com.icesoft.msdb.telegram.bot.repository.mongo.SubscriptionsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SubscriptionsService {

    @Autowired
    private lateinit var subscriptionsRepository: SubscriptionsRepository

    @Autowired
    private lateinit var seriesRepository: SeriesRepository

    fun getSeries(): List<Series> = seriesRepository.findAllByOrderByRelevanceAsc().toList()

    fun subscribeChatToSeries(chatId: Long, seriesId: Long, notifyRaces: Boolean, notifyQualifying: Boolean, notifyPractices: Boolean) {
        val telegramGroupSubscription = subscriptionsRepository.findById(seriesId)
            .orElse(TelegramGroupSubscription(seriesId, chatId))
        telegramGroupSubscription.seriesName = getSeries().first { series -> series.id == seriesId }.name
        telegramGroupSubscription.notifyRaces = notifyRaces
        telegramGroupSubscription.notifyQualifying = notifyQualifying
        telegramGroupSubscription.notifyPractice = notifyPractices
        subscriptionsRepository.save(telegramGroupSubscription)
    }

    fun getSubscriptions(chatId: Long): List<TelegramGroupSubscription> = subscriptionsRepository.findByChatId(chatId)

    fun unsubscribe(chatId: Long, seriesId: Long) = subscriptionsRepository.delete(TelegramGroupSubscription(seriesId, chatId))
}