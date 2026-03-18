package com.yaropaul.mynews.domain.usecase

import com.yaropaul.mynews.domain.NewsDefaults
import com.yaropaul.mynews.domain.model.Article
import com.yaropaul.mynews.domain.repository.NewsRepository
import com.yaropaul.mynews.utils.LanguageProvider
import javax.inject.Inject

class GetTopHeadlinesUseCase @Inject constructor(
    private val repository: NewsRepository,
    private val languageProvider: LanguageProvider
) {
    /**
     * Fetches top headlines using a 3-step cascade fallback:
     *   1. device country + device language  (e.g. country=fr, language=fr)
     *   2. device country only               (e.g. country=fr — drops language filter)
     *   3. global fallback                   (country=us, language=en)
     *
     * Stops as soon as a non-empty result is returned.
     * Never performs more than 3 network calls per invocation.
     */
    suspend operator fun invoke(): List<Article> {
        val country = languageProvider.getCountry()
        val language = languageProvider.getLanguage()

        // Attempt 1 — locale-specific
        var articles = repository.getTopHeadlines(country = country, language = language)
        if (articles.isNotEmpty()) return articles

        // Attempt 2 — country only (no language filter)
        articles = repository.getTopHeadlines(country = country, language = null)
        if (articles.isNotEmpty()) return articles

        // Attempt 3 — global US/EN fallback
        return repository.getTopHeadlines(
            country = NewsDefaults.FALLBACK_COUNTRY,
            language = NewsDefaults.FALLBACK_LANGUAGE
        )
    }
}
