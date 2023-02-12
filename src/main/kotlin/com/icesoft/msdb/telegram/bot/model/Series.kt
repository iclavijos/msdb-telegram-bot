package com.icesoft.msdb.telegram.bot.model

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Series(
    @Id var id: Long,
    var name: String,
    var shortname: String,
    var logoUrl: String,
    var organizer: String,
    var relevance: Int
) {
    constructor() : this(
        -1, "",
        "", "",
        "", -1
    )
}