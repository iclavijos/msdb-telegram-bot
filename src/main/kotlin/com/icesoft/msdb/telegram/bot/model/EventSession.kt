package com.icesoft.msdb.telegram.bot.model

data class EventSession(
    var id: Long,
    var name: String,
    var shortname: String,
    var sessionStartTime: Long,
    var sessionType: String,
    var race: Boolean = false,
    var eventEdition: EventEdition
)
