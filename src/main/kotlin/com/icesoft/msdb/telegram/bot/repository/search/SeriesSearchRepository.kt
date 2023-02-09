package com.icesoft.msdb.telegram.bot.repository.search

import com.icesoft.msdb.telegram.bot.model.Series
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface SeriesSearchRepository: ElasticsearchRepository<Series, Long> {
}