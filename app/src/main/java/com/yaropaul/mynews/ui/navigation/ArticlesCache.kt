package com.yaropaul.mynews.ui.navigation

import com.yaropaul.mynews.domain.model.Article
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory cache for the last successfully loaded list of articles.
 *
 * Used by [com.yaropaul.mynews.presentation.detail.DetailViewModel] to resolve an article
 * by URL after the user navigates to the detail screen.
 *
 * Lifecycle: Hilt singleton — lives as long as the application process.
 *
 * Limitation: not persisted across process death. If the OS kills the app while the user
 * is on DetailScreen and the system restores the back-stack, [findByUrl] will return null.
 * DetailViewModel handles this case by emitting
 * [com.yaropaul.mynews.presentation.detail.DetailUiState.Unavailable],
 * which triggers automatic back-navigation.
 *
 * If a persistent solution is required in the future (deep links, offline), replace this
 * cache with a Room-backed repository and query by URL in DetailViewModel.
 */
@Singleton
class ArticlesCache @Inject constructor() {

    @Volatile
    private var _articles: List<Article> = emptyList()

    fun updateArticles(articles: List<Article>) {
        _articles = articles
    }

    fun findByUrl(url: String): Article? = _articles.find { it.url == url }
}
