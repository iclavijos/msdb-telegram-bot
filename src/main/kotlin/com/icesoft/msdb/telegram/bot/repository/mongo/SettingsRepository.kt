package com.icesoft.msdb.telegram.bot.repository.mongo

import com.icesoft.msdb.telegram.bot.model.TelegramGroupSettings
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface SettingsRepository : MongoRepository<TelegramGroupSettings, Long>