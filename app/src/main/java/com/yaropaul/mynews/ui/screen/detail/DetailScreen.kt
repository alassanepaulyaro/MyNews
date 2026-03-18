package com.yaropaul.mynews.ui.screen.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yaropaul.mynews.presentation.detail.DetailUiState
import com.yaropaul.mynews.presentation.detail.DetailViewModel

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is DetailUiState.Loaded -> DetailContent(
            article = state.article,
            onBack = onBack
        )
        is DetailUiState.Unavailable -> {
            // Article lost after process death — navigate back immediately.
            LaunchedEffect(Unit) { onBack() }
        }
    }
}
