package com.yaropaul.mynews.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaropaul.mynews.domain.usecase.GetTopHeadlinesUseCase
import com.yaropaul.mynews.ui.navigation.ArticlesCache
import com.yaropaul.mynews.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getTopHeadlines: GetTopHeadlinesUseCase,
    private val articlesCache: ArticlesCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadHeadlines(isRefresh = false)
    }

    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.Refresh -> loadHeadlines(isRefresh = true)
        }
    }

    private fun loadHeadlines(isRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { state ->
                // Swipe-to-refresh over existing content: show the indicator and keep the
                // current list/error visible underneath.
                // Initial load (or pull during initial load): replace with the full-screen spinner.
                if (isRefresh && state.newsState !is NewsUiState.Loading) {
                    state.copy(isRefreshing = true)
                } else {
                    state.copy(newsState = NewsUiState.Loading, isRefreshing = false)
                }
            }
            try {
                val articles = getTopHeadlines()
                articlesCache.updateArticles(articles)
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        newsState = if (articles.isEmpty()) NewsUiState.Empty
                        else NewsUiState.Success(articles)
                    )
                }
            } catch (e: CancellationException) {
                // Coroutine was cancelled (e.g. rapid swipe) — do not update UI state.
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isRefreshing = false, newsState = NewsUiState.Error(e.toUserMessage()))
                }
            }
        }
    }
}
