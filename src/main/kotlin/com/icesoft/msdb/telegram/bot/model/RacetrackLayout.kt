package com.icesoft.msdb.telegram.bot.model

data class RacetrackLayout(
    var id: Long,
    var name: String,
    var layoutImageUrl: String?,
    var racetrack: Racetrack
)
