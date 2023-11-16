package com.icesoft.msdb.telegram.bot.model

import java.time.LocalDate

data class EventEdition(
    var id: Long,
    var longEventName: String,
    var eventDate: LocalDate,
    var posterUrl: String?,
    var trackLayout: RacetrackLayout?,
    var locationTimeZone: String?,
    var event: Event
)
