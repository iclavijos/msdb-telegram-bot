package com.icesoft.msdb.telegram.bot.repository

import com.icesoft.msdb.telegram.bot.model.Series
import org.springframework.data.jpa.repository.JpaRepository

interface SeriesRepository : JpaRepository<Series, Long> {

    fun findAllByOrderByRelevanceAsc(): Iterable<Series>
}