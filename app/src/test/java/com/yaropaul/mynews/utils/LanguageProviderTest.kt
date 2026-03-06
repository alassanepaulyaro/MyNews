package com.yaropaul.mynews.utils

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class LanguageProviderTest {

    private lateinit var originalLocale: Locale
    private lateinit var provider: LanguageProvider

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
        provider = LanguageProvider()
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    // --- getLanguage ---

    @Test
    fun `getLanguage returns device language when supported`() {
        Locale.setDefault(Locale.forLanguageTag("fr-FR"))
        assertEquals("fr", provider.getLanguage())
    }

    @Test
    fun `getLanguage returns en for unsupported language`() {
        Locale.setDefault(Locale.forLanguageTag("xx-XX"))
        assertEquals("en", provider.getLanguage())
    }

    @Test
    fun `getLanguage returns en when language tag is empty`() {
        Locale.setDefault(Locale.forLanguageTag("und-US"))
        assertEquals("en", provider.getLanguage())
    }

    @Test
    fun `getLanguage returns he for Hebrew locale`() {
        Locale.setDefault(Locale.forLanguageTag("he-IL"))
        assertEquals("he", provider.getLanguage())
    }

    // --- getCountry ---

    @Test
    fun `getCountry returns device country when supported`() {
        Locale.setDefault(Locale.forLanguageTag("en-GB"))
        assertEquals("gb", provider.getCountry())
    }

    @Test
    fun `getCountry returns us for unsupported country`() {
        Locale.setDefault(Locale.forLanguageTag("xx-XX"))
        assertEquals("us", provider.getCountry())
    }

    @Test
    fun `getCountry returns il for Israel locale`() {
        Locale.setDefault(Locale.forLanguageTag("he-IL"))
        assertEquals("il", provider.getCountry())
    }

    @Test
    fun `getCountry returns us when country tag is empty`() {
        Locale.setDefault(Locale.forLanguageTag("en"))
        assertEquals("us", provider.getCountry())
    }
}
