package com.flash.contentfilterapi.service

import org.springframework.stereotype.Service

@Service
class ContentFilterService(private val restrictedWordService: RestrictedWordService) {

    fun filterContent(string: String): String {
        return try {
            val restrictedWords = restrictedWordService.getWords()

            if (restrictedWords.isEmpty()) {
                return string
            }
            val sortedRestrictedWords = restrictedWords.sortedByDescending { it.length }
            val pattern = Regex(sortedRestrictedWords.joinToString("|") { "(?<!\\w)${Regex.escape(it)}(?!\\w)" }, RegexOption.IGNORE_CASE)

            pattern.replace(string) { matchResult ->
                "*".repeat(matchResult.value.length)
            }
        } catch (e: Exception) {
            throw ContentFilterException("Error sanitizing words", e)
        }
    }
}

/**
 * Exception thrown when an error occurs in the Content Filter Service.
 *
 * @param message Detailed message about the exception.
 * @param cause The root cause of the exception.
 */
class ContentFilterException(message: String, cause: Throwable): RuntimeException(message, cause)