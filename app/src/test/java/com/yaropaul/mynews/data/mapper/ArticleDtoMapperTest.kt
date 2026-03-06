package com.yaropaul.mynews.data.mapper

import com.yaropaul.mynews.data.remote.dto.ArticleDto
import com.yaropaul.mynews.data.remote.dto.SourceDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArticleDtoMapperTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = ArticleDto(
            source = SourceDto(id = "nyt", name = "NY Times"),
            author = "John Doe",
            title = "Test Title",
            description = "Test description",
            url = "https://example.com",
            urlToImage = "https://example.com/img.jpg",
            publishedAt = "2024-01-01T00:00:00Z",
            content = "Test content"
        )

        val article = dto.toDomain()

        assertEquals("nyt", article?.sourceId)
        assertEquals("NY Times", article?.sourceName)
        assertEquals("John Doe", article?.author)
        assertEquals("Test Title", article?.title)
        assertEquals("Test description", article?.description)
        assertEquals("https://example.com", article?.url)
        assertEquals("https://example.com/img.jpg", article?.urlToImage)
        assertEquals("2024-01-01T00:00:00Z", article?.publishedAt)
        assertEquals("Test content", article?.content)
    }

    @Test
    fun `toDomain maps null optional fields to empty strings`() {
        val dto = ArticleDto(
            source = SourceDto(id = null, name = null),
            author = null,
            title = "A title",
            description = null,
            url = "https://example.com",
            urlToImage = null,
            publishedAt = null,
            content = null
        )

        val article = dto.toDomain()

        assertEquals("", article?.sourceId)
        assertEquals("", article?.sourceName)
        assertEquals("", article?.author)
        assertEquals("", article?.description)
        assertEquals("", article?.urlToImage)
        assertEquals("", article?.publishedAt)
        assertEquals("", article?.content)
    }

    @Test
    fun `toDomain returns null when url is blank`() {
        val dto = ArticleDto(
            source = null,
            author = "Author",
            title = "Title",
            description = "Desc",
            url = "",
            urlToImage = null,
            publishedAt = null,
            content = null
        )

        assertNull(dto.toDomain())
    }

    @Test
    fun `toDomain returns null for removed placeholder articles`() {
        val dto = ArticleDto(
            source = null,
            author = null,
            title = "[Removed]",
            description = null,
            url = "https://example.com",
            urlToImage = null,
            publishedAt = null,
            content = null
        )

        assertNull(dto.toDomain())
    }
}
