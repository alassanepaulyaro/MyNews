package com.yaropaul.mynews.utils

import com.yaropaul.mynews.R
import com.yaropaul.mynews.ui.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ExtensionsTest {

    @Test
    fun `IOException returns string resource for no internet`() {
        val result = IOException().toUserMessage()
        assertTrue(result is UiText.StringResource)
        assertEquals(R.string.error_no_internet, (result as UiText.StringResource).resId)
    }

    @Test
    fun `generic exception with message returns dynamic string`() {
        val result = RuntimeException("Something went wrong").toUserMessage()
        assertTrue(result is UiText.DynamicString)
        assertEquals("Something went wrong", (result as UiText.DynamicString).value)
    }

    @Test
    fun `generic exception with blank message returns string resource for unexpected error`() {
        val result = RuntimeException("   ").toUserMessage()
        assertTrue(result is UiText.StringResource)
        assertEquals(R.string.error_unexpected, (result as UiText.StringResource).resId)
    }

    @Test
    fun `generic exception with null message returns string resource for unexpected error`() {
        val result = RuntimeException().toUserMessage()
        assertTrue(result is UiText.StringResource)
        assertEquals(R.string.error_unexpected, (result as UiText.StringResource).resId)
    }
}
