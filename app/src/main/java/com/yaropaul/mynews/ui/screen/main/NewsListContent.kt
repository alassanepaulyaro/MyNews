package com.yaropaul.mynews.ui.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yaropaul.mynews.domain.model.Article
import com.yaropaul.mynews.presentation.main.NewsUiState
import com.yaropaul.mynews.ui.components.ErrorView
import com.yaropaul.mynews.ui.components.LoadingView

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
            message = "No articles available right now.",
            onRetry = onRetry,
            modifier = modifier
        )
        is NewsUiState.Success -> {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
