package com.icesoft.msdb.telegram.bot.client

import com.icesoft.msdb.telegram.bot.model.EventEdition
import com.icesoft.msdb.telegram.bot.model.EventSession
import com.icesoft.msdb.telegram.bot.model.Series
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "eventEditionsClient", url = "https://www.motorsports-database.racing/api/event-editions", decode404 = true)
interface EventEditionClient {

    @GetMapping("/event-sessions/{seriesId}/nextSession")
    fun getNextSession(@PathVariable seriesId: Long): EventSession?
}