package com.yaropaul.mynews.ui.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yaropaul.mynews.R
import com.yaropaul.mynews.domain.model.Article
import com.yaropaul.mynews.presentation.main.NewsUiState
import com.yaropaul.mynews.ui.UiText
import com.yaropaul.mynews.ui.components.ErrorView
import com.yaropaul.mynews.ui.components.LoadingView
import com.yaropaul.mynews.ui.theme.Dimens

@Composable
fun NewsListContent(
    newsState: NewsUiState,
    onArticleClick: (Article) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (newsState) {
        is NewsUiState.Loading -> LoadingView(modifier = modifier)
        is NewsUiState.Error -> ErrorView(
            message = newsState.message,
            onRetry = onRetry,
            modifier = modifier
        )
        is NewsUiState.Empty -> ErrorView(
            message = UiText.StringResource(R.string.error_no_articles),
            onRetry = onRetry,
            modifier = modifier
        )
        is NewsUiState.Success -> {
            val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(
                    start = Dimens.ScreenPaddingHorizontal,
                    top = Dimens.ScreenPaddingHorizontal,
                    end = Dimens.ScreenPaddingHorizontal,
                    bottom = Dimens.ScreenPaddingHorizontal + navBarBottom
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing)
            ) {
                items(
                    items = newsState.articles,
                    key = { it.url }
                ) { article ->
                    NewsCard(
                        article = article,
                        onClick = { onArticleClick(article) }
                    )
                }
            }
        }
    }
}
