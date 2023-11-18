package com.icesoft.msdb.telegram.bot.model

data class EventAndWinners(
    var eventEdition: EventEdition?
) {
    constructor() : this(
        null
    )
}
