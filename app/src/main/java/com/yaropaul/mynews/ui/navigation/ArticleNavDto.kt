package com.yaropaul.mynews.ui.navigation

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.yaropaul.mynews.domain.model.Article

@JsonClass(generateAdapter = true)
data class ArticleNavDto(
    @Json(name = "sourceId") val sourceId: String,
    @Json(name = "sourceName") val sourceName: String,
    @Json(name = "author") val author: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "url") val url: String,
    @Json(name = "urlToImage") val urlToImage: String,
    @Json(name = "publishedAt") val publishedAt: String,
    @Json(name = "content") val content: String
)

fun Article.toNavDto(): ArticleNavDto = ArticleNavDto(
    sourceId = sourceId,
    sourceName = sourceName,
    author = author,
    title = title,
    description = description,
    url = url,
    urlToImage = urlToImage,
    publishedAt = publishedAt,
    content = content
)

fun ArticleNavDto.toArticle(): Article = Article(
    sourceId = sourceId,
    sourceName = sourceName,
    author = author,
    title = title,
    description = description,
    url = url,
    urlToImage = urlToImage,
    publishedAt = publishedAt,
    content = content
)
