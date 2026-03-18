package com.yaropaul.mynews.presentation.detail

import com.yaropaul.mynews.domain.model.Article

sealed interface DetailUiState {
    /** The article is available and ready to display. */
    data class Loaded(val article: Article) : DetailUiState

    /**
     * Article URL was resolved but not found in [com.yaropaul.mynews.ui.navigation.ArticlesCache].
     * This happens after process death: the URL is preserved in the navigation back-stack via
     * SavedStateHandle, but the in-memory cache is empty.
     * The UI should trigger automatic back-navigation when it observes this state.
     */
    data object Unavailable : DetailUiState
}
