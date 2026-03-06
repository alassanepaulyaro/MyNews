package com.yaropaul.mynews.data.repository

import com.yaropaul.mynews.data.mapper.toDomain
import com.yaropaul.mynews.data.remote.api.NewsApiService
import com.yaropaul.mynews.domain.model.Article
import com.yaropaul.mynews.domain.repository.NewsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val apiService: NewsApiService
) : NewsRepository {

    override suspend fun getTopHeadlines(
        country: String,
        language: String?
    ): List<Article> {
        return apiService.getTopHeadlines(
            country = country,
            language = language
        ).articles.mapNotNull { it.toDomain() }
    }
}
