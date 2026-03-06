package com.yaropaul.mynews.domain.repository

import com.yaropaul.mynews.domain.model.Article

interface NewsRepository {
    suspend fun getTopHeadlines(country: String, language: String? = null): List<Article>
}
