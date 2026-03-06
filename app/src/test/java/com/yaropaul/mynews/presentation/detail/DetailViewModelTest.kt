package com.yaropaul.mynews.presentation.detail

import androidx.lifecycle.SavedStateHandle
import java.net.URLEncoder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.yaropaul.mynews.ui.navigation.ArticleNavDto
import com.yaropaul.mynews.ui.navigation.NavRoutes
import com.yaropaul.mynews.ui.navigation.toNavDto
import com.yaropaul.mynews.util.fakeArticle
import com.yaropaul.mynews.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(ArticleNavDto::class.java)

    private fun createViewModel(
        articleUrl: String = "https://example.com/article/1"
    ): DetailViewModel {
        val article = fakeArticle(url = articleUrl)
        val json = adapter.toJson(article.toNavDto())
        val encoded = URLEncoder.encode(json, "UTF-8")
        val savedStateHandle = SavedStateHandle(
            mapOf(NavRoutes.ARG_ARTICLE to encoded)
        )
        return DetailViewModel(savedStateHandle)
    }

    @Test
    fun `initial state has correct article from JSON nav arg`() = runTest {
        val viewModel = createViewModel()
        assertEquals("https://example.com/article/1", viewModel.uiState.value.article.url)
    }

    @Test
    fun `article fields are correctly deserialized`() = runTest {
        val viewModel = createViewModel()
        val article = viewModel.uiState.value.article
        assertEquals("Test Article Title", article.title)
        assertEquals("Test Author", article.author)
        assertEquals("Test Source", article.sourceName)
    }
}
