package com.yaropaul.mynews.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaropaul.mynews.domain.usecase.GetTopHeadlinesUseCase
import com.yaropaul.mynews.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getTopHeadlines: GetTopHeadlinesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadHeadlines()
    }

    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.Refresh -> loadHeadlines()
        }
    }

    private fun loadHeadlines() {
        viewModelScope.launch {
            _uiState.update { it.copy(newsState = NewsUiState.Loading) }
            try {
                val articles = getTopHeadlines()
                _uiState.update {
                    it.copy(
                        newsState = if (articles.isEmpty()) NewsUiState.Empty
                        else NewsUiState.Success(articles)
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(newsState = NewsUiState.Error(e.toUserMessage())) }
            }
        }
    }
}
