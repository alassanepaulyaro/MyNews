package com.yaropaul.mynews.ui.navigation

import android.net.Uri

object NavRoutes {
    const val MAIN = "main"

    // Argument key carried in the detail route.
    const val DETAIL_ARG = "articleUrl"

    // Route pattern: detail/{articleUrl}
    const val DETAIL = "detail/{$DETAIL_ARG}"

    /** Builds the concrete detail route for a given article URL (URL-encoded). */
    fun detailRoute(articleUrl: String): String = "detail/${Uri.encode(articleUrl)}"
}
