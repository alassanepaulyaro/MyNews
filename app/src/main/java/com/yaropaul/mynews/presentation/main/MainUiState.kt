package com.yaropaul.mynews.presentation.main

import com.yaropaul.mynews.domain.model.Article

data class MainUiState(
    val newsState: NewsUiState = NewsUiState.Loading
)

sealed interface NewsUiState {
    data object Loading : NewsUiState
    data class Success(val articles: List<Article>) : NewsUiState
    data object Empty : NewsUiState
    data class Error(val message: String) : NewsUiState
}
