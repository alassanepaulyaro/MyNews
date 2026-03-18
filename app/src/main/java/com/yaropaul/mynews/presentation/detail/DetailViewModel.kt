package com.yaropaul.mynews.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.yaropaul.mynews.ui.navigation.ArticlesCache
import com.yaropaul.mynews.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    articlesCache: ArticlesCache
) : ViewModel() {

    // The article URL is a required navigation argument — always present in SavedStateHandle
    // when navigated to via AppNavGraph. Preserved across process death by the nav back-stack.
    private val articleUrl: String = checkNotNull(savedStateHandle[NavRoutes.DETAIL_ARG])

    private val _uiState = MutableStateFlow<DetailUiState>(
        articlesCache.findByUrl(articleUrl)
            ?.let { DetailUiState.Loaded(it) }
            ?: DetailUiState.Unavailable
    )
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
}
