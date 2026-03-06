package com.yaropaul.mynews.data.mapper

import com.yaropaul.mynews.data.remote.dto.ArticleDto
import com.yaropaul.mynews.domain.model.Article

private const val REMOVED_PLACEHOLDER = "[Removed]"

fun ArticleDto.toDomain(): Article? {
    val safeUrl = url.orEmpty()
    val safeTitle = title.orEmpty()
    // Filter out NewsAPI placeholder "removed" articles
    if (safeUrl.isBlank() || safeTitle == REMOVED_PLACEHOLDER) return null
    return Article(
        sourceId = source?.id.orEmpty(),
        sourceName = source?.name.orEmpty(),
        author = author.orEmpty(),
        title = safeTitle,
        description = description.orEmpty(),
        url = safeUrl,
        urlToImage = urlToImage.orEmpty(),
        publishedAt = publishedAt.orEmpty(),
        content = content.orEmpty()
    )
}
