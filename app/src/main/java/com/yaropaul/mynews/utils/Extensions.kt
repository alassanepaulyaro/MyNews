package com.yaropaul.mynews.utils

import com.yaropaul.mynews.R
import com.yaropaul.mynews.ui.UiText
import retrofit2.HttpException
import java.io.IOException

fun Throwable.toUserMessage(): UiText = when (this) {
    is IOException -> UiText.StringResource(R.string.error_no_internet)
    is HttpException -> when (code()) {
        401 -> UiText.StringResource(R.string.error_unauthorized)
        429 -> UiText.StringResource(R.string.error_rate_limit)
        in 500..599 -> UiText.StringResource(R.string.error_server)
        else -> UiText.StringResource(R.string.error_unexpected)
    }
    else -> message?.takeIf { it.isNotBlank() }
        ?.let { UiText.DynamicString(it) }
        ?: UiText.StringResource(R.string.error_unexpected)
}
