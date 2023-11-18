package com.icesoft.msdb.telegram.bot.client

import com.icesoft.msdb.telegram.bot.model.EventAndWinners
import com.icesoft.msdb.telegram.bot.model.SeriesEdition
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "seriesEditionClient", url = "\${app.server}/api/series-editions")
interface SeriesEditionClient {

    @GetMapping("/{seriesEditionId}")
    fun getSeriesEdition(@PathVariable seriesEditionId: Long): SeriesEdition

    @GetMapping("/{seriesEditionId}/events")
    fun getSeriesEditionEvents(@PathVariable seriesEditionId: Long): List<EventAndWinners>
}