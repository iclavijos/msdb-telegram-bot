package com.icesoft.msdb.telegram.bot.model

import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import javax.persistence.Entity
import javax.persistence.Id

@Entity
@Document(indexName = "series")
data class Series(
    @Id var id: Long,
    @Field(type = FieldType.Search_As_You_Type)
    var name: String,
    @Field(type = FieldType.Search_As_You_Type)
    var shortname: String,
    var logoUrl: String,
    @Field(type = FieldType.Search_As_You_Type)
    var organizer: String,
    var relevance: Int
) {
}