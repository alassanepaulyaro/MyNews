package com.yaropaul.mynews.presentation.main

import app.cash.turbine.test
import com.yaropaul.mynews.domain.usecase.GetTopHeadlinesUseCase
import com.yaropaul.mynews.util.MainDispatcherRule
import com.yaropaul.mynews.util.fakeArticle
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTopHeadlines: GetTopHeadlinesUseCase = mockk()

    private fun createViewModel() = MainViewModel(getTopHeadlines)

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
    fun `API error emits error state`() = runTest {
        coEvery { getTopHeadlines() } throws RuntimeException("Network error")

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.newsState is NewsUiState.Error)
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
