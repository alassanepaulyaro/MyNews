package com.yaropaul.mynews.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.yaropaul.mynews.domain.model.Article
import com.yaropaul.mynews.ui.navigation.ArticleNavDto
import com.yaropaul.mynews.ui.navigation.NavRoutes
import com.yaropaul.mynews.ui.navigation.toArticle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val article: Article = run {
        val raw: String = checkNotNull(savedStateHandle[NavRoutes.ARG_ARTICLE]) {
            "Missing article navigation argument"
        }
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(ArticleNavDto::class.java)
        val decoded = java.net.URLDecoder.decode(raw, "UTF-8")
        requireNotNull(adapter.fromJson(decoded)?.toArticle()) {
            "Failed to parse Article from navigation argument"
        }
    }

    private val _uiState = MutableStateFlow(DetailUiState(article = article))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
}
