package com.yaropaul.mynews.ui.navigation

object NavRoutes {
    const val MAIN = "main"
    const val ARG_ARTICLE = "article"
    const val DETAIL = "detail/{$ARG_ARTICLE}"
    fun detailRoute(encodedArticleJson: String) = "detail/$encodedArticleJson"
}
