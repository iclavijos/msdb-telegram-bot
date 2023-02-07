package com.icesoft.msdb.telegram.bot.config

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import java.util.*

@org.springframework.context.annotation.Configuration
class FreeMarkerConfiguration {

    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Bean
    fun freemarkerConfiguration(): Configuration {
        var cfg = Configuration(Configuration.VERSION_2_3_32)
        cfg.setDirectoryForTemplateLoading(resourceLoader.getResource("classpath:templates").file)
        cfg.defaultEncoding = "UTF-8"
        cfg.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        cfg.logTemplateExceptions = false
        cfg.wrapUncheckedExceptions = true
        cfg.fallbackOnNullLoopVariable = false
        cfg.sqlDateAndTimeTimeZone = TimeZone.getDefault()

        return cfg
    }
}