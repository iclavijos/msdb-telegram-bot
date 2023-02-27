package com.icesoft.msdb.telegram.bot.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableJpaRepositories("com.icesoft.msdb.telegram.bot.repository.jpa")
@EnableMongoRepositories("com.icesoft.msdb.telegram.bot.repository.mongo")
class ApplicationConfiguration {

}