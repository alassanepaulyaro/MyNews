package com.yaropaul.mynews.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yaropaul.mynews.R
import com.yaropaul.mynews.presentation.main.MainUiEvent
import com.yaropaul.mynews.presentation.main.MainViewModel
import com.yaropaul.mynews.ui.components.ScreenSurface
import com.yaropaul.mynews.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToDetail: (articleUrl: String) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenSurface {
        // Header — title only, refresh is now done via swipe gesture
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.ScreenPaddingHorizontal,
                    vertical = Dimens.ScreenPaddingVertical
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider(thickness = Dimens.DividerThickness)

        // PullToRefreshBox owns the swipe gesture and the spinner indicator.
        // isRefreshing is false during initial load (NewsUiState.Loading handles that).
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onEvent(MainUiEvent.Refresh) },
            modifier = Modifier.fillMaxSize()
        ) {
            NewsListContent(
                newsState = uiState.newsState,
                onArticleClick = { article -> onNavigateToDetail(article.url) },
                onRetry = { viewModel.onEvent(MainUiEvent.Refresh) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
