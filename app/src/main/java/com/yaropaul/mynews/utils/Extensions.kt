package com.yaropaul.mynews.utils

import java.io.IOException

fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "No internet connection. Please check your network."
    else -> message?.takeIf { it.isNotBlank() } ?: "An unexpected error occurred."
}
