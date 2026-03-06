package com.yaropaul.mynews.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SourceDto(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?
)
