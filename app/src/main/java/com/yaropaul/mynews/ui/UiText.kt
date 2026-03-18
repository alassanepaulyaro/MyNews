package com.yaropaul.mynews.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Abstraction for UI-displayable text that can originate from either a hardcoded string
 * (e.g. a server error message) or a localised string resource.
 *
 * Resolving to a [String] requires either a [Context] (non-Compose) or a Composable context.
 */
sealed class UiText {
    /** A runtime string, e.g. a server-provided error message. */
    data class DynamicString(val value: String) : UiText()

    /** A localised string from res/values/strings.xml. */
    data class StringResource(@StringRes val resId: Int) : UiText()

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(resId)
    }

    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(resId)
    }
}
