package com.icesoft.msdb.telegram.bot.client

import com.icesoft.msdb.telegram.bot.model.Series
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "seriesClient", url = "\${app.server}/api/series")
interface SeriesClient {

    @GetMapping("/{seriesId}")
    fun getSeries(@PathVariable seriesId: Long): Series

    @GetMapping
    fun getSeries(@RequestParam(required = false) query: String): List<Series>

    @GetMapping
    fun getSeries(@RequestParam(required = false) query: String, pageable: Pageable): List<Series>
}