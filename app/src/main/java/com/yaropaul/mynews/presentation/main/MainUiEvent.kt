package com.yaropaul.mynews.presentation.main

sealed interface MainUiEvent {
    data object Refresh : MainUiEvent
}
