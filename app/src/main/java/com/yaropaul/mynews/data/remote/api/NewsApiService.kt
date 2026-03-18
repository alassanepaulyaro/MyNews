package com.yaropaul.mynews.data.remote.api

import com.yaropaul.mynews.data.remote.NewsApiConfig
import com.yaropaul.mynews.data.remote.dto.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String,
        @Query("language") language: String? = null,
        @Query("pageSize") pageSize: Int = NewsApiConfig.DEFAULT_PAGE_SIZE
    ): NewsResponseDto
}
