package com.yaropaul.mynews.util

import com.yaropaul.mynews.domain.model.Article

fun fakeArticle(
    url: String = "https://example.com/article/1",
    title: String = "Test Article Title",
    description: String = "Test description",
    author: String = "Test Author",
    sourceName: String = "Test Source",
    urlToImage: String = "https://example.com/image.jpg",
    content: String = "Test content"
) = Article(
    sourceId = "test-source",
    sourceName = sourceName,
    author = author,
    title = title,
    description = description,
    url = url,
    urlToImage = urlToImage,
    publishedAt = "2024-01-01T00:00:00Z",
    content = content
)
