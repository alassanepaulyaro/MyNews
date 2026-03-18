package com.yaropaul.mynews.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.yaropaul.mynews.ui.navigation.ArticlesCache
import com.yaropaul.mynews.ui.navigation.NavRoutes
import com.yaropaul.mynews.util.MainDispatcherRule
import com.yaropaul.mynews.util.fakeArticle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        articleUrl: String = "https://example.com/article/1"
    ): DetailViewModel {
        val article = fakeArticle(url = articleUrl)
        val cache = ArticlesCache().also { it.updateArticles(listOf(article)) }
        val savedStateHandle = SavedStateHandle(mapOf(NavRoutes.DETAIL_ARG to articleUrl))
        return DetailViewModel(savedStateHandle, cache)
    }

    @Test
    fun `initial state exposes the selected article`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertTrue(state is DetailUiState.Loaded)
        assertEquals("https://example.com/article/1", (state as DetailUiState.Loaded).article.url)
    }

    @Test
    fun `article fields are correctly forwarded`() = runTest {
        val viewModel = createViewModel()
        val article = (viewModel.uiState.value as DetailUiState.Loaded).article
        assertEquals("Test Article Title", article.title)
        assertEquals("Test Author", article.author)
        assertEquals("Test Source", article.sourceName)
    }

    @Test
    fun `emits Unavailable when article not found in cache (process death)`() {
        val emptyCache = ArticlesCache()
        val savedStateHandle = SavedStateHandle(
            mapOf(NavRoutes.DETAIL_ARG to "https://example.com/article/1")
        )
        val viewModel = DetailViewModel(savedStateHandle, emptyCache)
        assertTrue(viewModel.uiState.value is DetailUiState.Unavailable)
    }
}
