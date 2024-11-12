package com.flash.contentfilterapi.service

import com.flash.contentfilterapi.model.RestrictedWord
import com.flash.contentfilterapi.repository.RestrictedWordRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RestrictedWordService(private val restrictedWordRepository: RestrictedWordRepository) {

    @Cacheable("restrictedWords")
    fun getWords(): Set<String> {
        return try {
            restrictedWordRepository.findAll().map { it.word.toString().lowercase() }.toSet()
        } catch (e: Exception) {
            throw RestrictedWordException("Error fetching words", e)
        }
    }

    @Transactional
    @CacheEvict(value = ["restrictedWords"], allEntries = true)
    fun addWord(word: String): RestrictedWord? {
        return try {
            val normalizedWord = word.lowercase()
            if (restrictedWordRepository.findByWord(normalizedWord) == null) {
                restrictedWordRepository.save(RestrictedWord().apply { this.word = normalizedWord })
            } else {
                null
            }
        } catch (e: Exception) {
            throw RestrictedWordException("Error adding word: $word", e)
        }
    }

    @Transactional
    @CacheEvict(value = ["restrictedWords"], allEntries = true)
    fun updateWord(oldWord: String, newWord: String): RestrictedWord? {
        return try {
            val normalizedOldWord = oldWord.lowercase()
            val normalizedNewWord = newWord.lowercase()
            val existingWord = restrictedWordRepository.findByWord(normalizedOldWord)
            if (existingWord != null) {
                existingWord.word = normalizedNewWord
                restrictedWordRepository.save(existingWord)
            } else {
                null
            }
        } catch (e: Exception) {
            throw RestrictedWordException("Error updating word from $oldWord to $newWord", e)
        }
    }

    @Transactional
    @CacheEvict(value = ["restrictedWords"], allEntries = true)
    fun deleteWord(word: String): Boolean {
        return try {
            val normalizedWord = word.lowercase()
            val existingWord = restrictedWordRepository.findByWord(normalizedWord)
            if (existingWord != null) {
                restrictedWordRepository.delete(existingWord)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            throw RestrictedWordException("Error deleting word: $word", e)
        }
    }

}

/**
 * Exception thrown when an error occurs in the Restricted Word Service.
 *
 * @param message Detailed message about the exception.
 * @param cause The root cause of the exception, if any.
 */
class RestrictedWordException(message: String, cause: Throwable): RuntimeException(message, cause)