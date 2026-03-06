package com.yaropaul.mynews.domain.usecase

import com.yaropaul.mynews.domain.repository.NewsRepository
import com.yaropaul.mynews.util.fakeArticle
import com.yaropaul.mynews.utils.LanguageProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTopHeadlinesUseCaseTest {

    private val repository: NewsRepository = mockk()
    private val languageProvider: LanguageProvider = mockk()
    private lateinit var useCase: GetTopHeadlinesUseCase

    @Before
    fun setUp() {
        useCase = GetTopHeadlinesUseCase(repository, languageProvider)
    }

    // --- Attempt 1 succeeds — no fallback ---

    @Test
    fun `returns articles from attempt 1 without fallback`() = runTest {
        val articles = listOf(fakeArticle())
        every { languageProvider.getLanguage() } returns "fr"
        every { languageProvider.getCountry() } returns "fr"
        coEvery { repository.getTopHeadlines(country = "fr", language = "fr") } returns articles

        val result = useCase()

        assertEquals(articles, result)
        coVerify(exactly = 1) { repository.getTopHeadlines(any(), any()) }
    }

    // --- Attempt 1 empty, attempt 2 (country-only) succeeds ---

    @Test
    fun `fallback to country-only when lang+country returns empty`() = runTest {
        val articles = listOf(fakeArticle())
        every { languageProvider.getLanguage() } returns "fr"
        every { languageProvider.getCountry() } returns "fr"
        coEvery { repository.getTopHeadlines(country = "fr", language = "fr") } returns emptyList()
        coEvery { repository.getTopHeadlines(country = "fr", language = null) } returns articles

        val result = useCase()

        assertEquals(articles, result)
        coVerify(exactly = 1) { repository.getTopHeadlines(country = "fr", language = "fr") }
        coVerify(exactly = 1) { repository.getTopHeadlines(country = "fr", language = null) }
        coVerify(exactly = 0) { repository.getTopHeadlines(country = "us", language = "en") }
    }

    // --- Attempts 1+2 empty, attempt 3 (US/EN) succeeds ---

    @Test
    fun `fallback to US when device locale returns empty on both attempts`() = runTest {
        val articles = listOf(fakeArticle())
        every { languageProvider.getLanguage() } returns "fr"
        every { languageProvider.getCountry() } returns "fr"
        coEvery { repository.getTopHeadlines(country = "fr", language = "fr") } returns emptyList()
        coEvery { repository.getTopHeadlines(country = "fr", language = null) } returns emptyList()
        coEvery { repository.getTopHeadlines(country = "us", language = "en") } returns articles

        val result = useCase()

        assertEquals(articles, result)
        coVerify(exactly = 3) { repository.getTopHeadlines(any(), any()) }
    }

    // --- All 3 attempts empty — returns empty list (ViewModel will show Empty state) ---

    @Test
    fun `returns empty list when all fallbacks return empty`() = runTest {
        every { languageProvider.getLanguage() } returns "fr"
        every { languageProvider.getCountry() } returns "fr"
        coEvery { repository.getTopHeadlines(any(), any()) } returns emptyList()

        val result = useCase()

        assertTrue(result.isEmpty())
        coVerify(exactly = 3) { repository.getTopHeadlines(any(), any()) }
    }
}
