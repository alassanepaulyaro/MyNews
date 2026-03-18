package com.yaropaul.mynews.data.repository

import com.yaropaul.mynews.data.remote.api.NewsApiService
import com.yaropaul.mynews.data.remote.dto.ArticleDto
import com.yaropaul.mynews.data.remote.dto.NewsResponseDto
import com.yaropaul.mynews.data.remote.dto.SourceDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NewsRepositoryImplTest {

    private val apiService: NewsApiService = mockk()
    private lateinit var repository: NewsRepositoryImpl

    @Before
    fun setUp() {
        repository = NewsRepositoryImpl(apiService)
    }

    @Test
    fun `getTopHeadlines maps DTOs to domain articles`() = runTest {
        val dto = ArticleDto(
            source = SourceDto(id = "bbc", name = "BBC"),
            author = "Reporter",
            title = "Breaking News",
            description = "Details here",
            url = "https://bbc.com/news/1",
            urlToImage = "https://bbc.com/img.jpg",
            publishedAt = "2024-01-01T00:00:00Z",
            content = "Full content"
        )
        coEvery {
            apiService.getTopHeadlines(any(), any(), any())
        } returns NewsResponseDto(status = "ok", totalResults = 1, articles = listOf(dto))

        val result = repository.getTopHeadlines(country = "us", language = "en")

        assertEquals(1, result.size)
        assertEquals("BBC", result[0].sourceName)
        assertEquals("Breaking News", result[0].title)
    }

    @Test
    fun `getTopHeadlines filters out removed articles`() = runTest {
        val removedDto = ArticleDto(
            source = null, author = null, title = "[Removed]",
            description = null, url = "https://example.com",
            urlToImage = null, publishedAt = null, content = null
        )
        coEvery {
            apiService.getTopHeadlines(any(), any(), any())
        } returns NewsResponseDto(status = "ok", totalResults = 1, articles = listOf(removedDto))

        val result = repository.getTopHeadlines(country = "us", language = "en")

        assertTrue(result.isEmpty())
    }
}
