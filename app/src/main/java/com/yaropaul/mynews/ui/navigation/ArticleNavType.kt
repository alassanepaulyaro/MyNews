package com.yaropaul.mynews.ui.navigation

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.yaropaul.mynews.domain.model.Article

private val moshi: Moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

private val adapter = moshi.adapter(ArticleNavDto::class.java)

val ArticleNavType = object : NavType<Article>(isNullableAllowed = false) {

    override fun get(bundle: Bundle, key: String): Article? {
        return bundle.getString(key)?.let { json ->
            adapter.fromJson(Uri.decode(json))?.toArticle()
        }
    }

    override fun parseValue(value: String): Article {
        return requireNotNull(adapter.fromJson(Uri.decode(value))?.toArticle()) {
            "Failed to parse Article from nav arg"
        }
    }

    override fun put(bundle: Bundle, key: String, value: Article) {
        bundle.putString(key, Uri.encode(adapter.toJson(value.toNavDto())))
    }

    override fun serializeAsValue(value: Article): String {
        return Uri.encode(adapter.toJson(value.toNavDto()))
    }
}
