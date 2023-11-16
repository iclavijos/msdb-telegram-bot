package com.icesoft.msdb.telegram.bot.model

data class Event(
    var id: Long,
    var name: String,
    var rally: Boolean,
    var raid: Boolean
)
