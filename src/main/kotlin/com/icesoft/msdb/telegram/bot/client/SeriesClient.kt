package com.icesoft.msdb.telegram.bot.client

import com.icesoft.msdb.telegram.bot.model.Series
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "seriesClient", url = "https://www.motorsports-database.racing/api/series")
interface SeriesClient {

    @GetMapping
    fun getSeries(@RequestParam(required = false) query: String): List<Series>
}