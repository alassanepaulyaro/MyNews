package com.yaropaul.mynews.presentation.main

import com.yaropaul.mynews.domain.model.Article
import com.yaropaul.mynews.ui.UiText

data class MainUiState(
    val newsState: NewsUiState = NewsUiState.Loading,
    /** True while a swipe-to-refresh is in progress. Drives the pull-to-refresh indicator. */
    val isRefreshing: Boolean = false
)

sealed interface NewsUiState {
    data object Loading : NewsUiState
    data class Success(val articles: List<Article>) : NewsUiState
    data object Empty : NewsUiState
    data class Error(val message: UiText) : NewsUiState
}
