package com.yaropaul.mynews.presentation.main

import app.cash.turbine.test
import com.yaropaul.mynews.ui.navigation.ArticlesCache
import com.yaropaul.mynews.domain.usecase.GetTopHeadlinesUseCase
import com.yaropaul.mynews.util.MainDispatcherRule
import com.yaropaul.mynews.util.fakeArticle
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTopHeadlines: GetTopHeadlinesUseCase = mockk()
    private val articlesCache = ArticlesCache()

    private fun createViewModel() = MainViewModel(getTopHeadlines, articlesCache)

    @Test
    fun `initial state loads headlines on launch`() = runTest {
        val articles = listOf(fakeArticle())
        coEvery { getTopHeadlines() } returns articles

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.newsState is NewsUiState.Success)
        assertEquals(articles, (state.newsState as NewsUiState.Success).articles)
    }

    @Test
    fun `initial load does not set isRefreshing`() = runTest {
        val articles = listOf(fakeArticle())
        coEvery { getTopHeadlines() } returns articles

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `successful load populates articles cache`() = runTest {
        val articles = listOf(fakeArticle())
        coEvery { getTopHeadlines() } returns articles

        createViewModel()
        advanceUntilIdle()

        assertEquals(articles, articlesCache.findByUrl(articles.first().url)?.let { listOf(it) })
    }

    @Test
    fun `API error emits error state`() = runTest {
        coEvery { getTopHeadlines() } throws RuntimeException("Network error")

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.newsState is NewsUiState.Error)
    }

    @Test
    fun `error clears isRefreshing`() = runTest {
        val articles = listOf(fakeArticle())
        coEvery { getTopHeadlines() } returns articles
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { getTopHeadlines() } throws RuntimeException("Network error")
        viewModel.onEvent(MainUiEvent.Refresh)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `empty result from all fallbacks emits Empty state`() = runTest {
        coEvery { getTopHeadlines() } returns emptyList()

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.newsState is NewsUiState.Empty)
    }

    @Test
    fun `refresh reloads headlines`() = runTest {
        val articles = listOf(fakeArticle())
        coEvery { getTopHeadlines() } returns articles

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(MainUiEvent.Refresh)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.newsState is NewsUiState.Success)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `swipe refresh sets isRefreshing then clears it on success`() = runTest {
        val articles = listOf(fakeArticle())
        coEvery { getTopHeadlines() } returns articles

        val viewModel = createViewModel()
        advanceUntilIdle() // initial load completes → Success

        viewModel.uiState.test {
            awaitItem() // current Success state

            viewModel.onEvent(MainUiEvent.Refresh)
            // isRefreshing = true emitted (newsState stays Success)
            val refreshing = awaitItem()
            assertTrue(refreshing.isRefreshing)
            assertTrue(refreshing.newsState is NewsUiState.Success)

            advanceUntilIdle()
            // isRefreshing = false, newsState = Success
            val done = awaitItem()
            assertFalse(done.isRefreshing)
            assertTrue(done.newsState is NewsUiState.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits loading before success`() = runTest {
        val articles = listOf(fakeArticle())
        coEvery { getTopHeadlines() } returns articles

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem().newsState is NewsUiState.Loading)

            advanceUntilIdle()
            assertTrue(awaitItem().newsState is NewsUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
