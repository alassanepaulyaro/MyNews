package com.yaropaul.mynews.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class ExtensionsTest {

    @Test
    fun `IOException returns no internet connection message`() {
        val e = IOException()
        assertEquals("No internet connection. Please check your network.", e.toUserMessage())
    }

    @Test
    fun `generic exception with message returns the message`() {
        val e = RuntimeException("Something went wrong")
        assertEquals("Something went wrong", e.toUserMessage())
    }

    @Test
    fun `generic exception with blank message returns fallback`() {
        val e = RuntimeException("   ")
        assertEquals("An unexpected error occurred.", e.toUserMessage())
    }

    @Test
    fun `generic exception with null message returns fallback`() {
        val e = RuntimeException()
        assertEquals("An unexpected error occurred.", e.toUserMessage())
    }
}
