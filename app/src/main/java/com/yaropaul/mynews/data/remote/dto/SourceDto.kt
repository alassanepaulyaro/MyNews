package com.yaropaul.mynews.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SourceDto(
    val id: String? = null,
    val name: String? = null
)
