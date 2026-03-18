package com.yaropaul.mynews.domain

/**
 * Domain-level fallback constants used by use-cases and utilities.
 * These belong to the domain layer and must not reference any data-layer classes.
 */
object NewsDefaults {
    const val FALLBACK_COUNTRY = "us"
    const val FALLBACK_LANGUAGE = "en"
}
