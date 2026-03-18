package com.yaropaul.mynews.di

import com.yaropaul.mynews.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that appends the API key as a query parameter to every outgoing request.
 * Centralises key injection so it never appears in individual service definitions.
 */
@Singleton
class ApiKeyInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("apiKey", BuildConfig.NEWS_API_KEY)
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }
}
