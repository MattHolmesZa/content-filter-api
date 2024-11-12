package com.flash.contentfilterapi

import com.flash.contentfilterapi.model.RestrictedWord
import com.flash.contentfilterapi.repository.RestrictedWordRepository
import com.flash.contentfilterapi.service.RestrictedWordException
import com.flash.contentfilterapi.service.RestrictedWordService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataAccessException

@SpringBootTest
class RestrictedWordServiceTest {

    private val restrictedWordRepository = mock(RestrictedWordRepository::class.java)
    private val restrictedWordService = RestrictedWordService(restrictedWordRepository)

    @Test
    fun `should return set of words from repository`() {
        val words = listOf(
            RestrictedWord().apply { word = "badword" },
            RestrictedWord().apply { word = "anotherbadword" }
        )
        `when`(restrictedWordRepository.findAll()).thenReturn(words)
        val result = restrictedWordService.getWords()

        assertEquals(setOf("badword", "anotherbadword"), result)
    }

    @Test
    fun `getWords should throw RestrictedWordException when repository throws exception`() {
        val exception = RuntimeException("Database error")
        `when`(restrictedWordRepository.findAll()).thenThrow(exception)
        val thrownException = assertThrows<RestrictedWordException> {
            restrictedWordService.getWords()
        }

        assertEquals("Error fetching words", thrownException.message)
        assertEquals(exception, thrownException.cause)
    }

    @Test
    fun `should add word when it does not exist`() {
        val word = "NewWord"
        val normalizedWord = word.lowercase()
        `when`(restrictedWordRepository.findByWord(normalizedWord)).thenReturn(null)
        val savedWord = RestrictedWord().apply { this.word = normalizedWord }
        `when`(restrictedWordRepository.save(any(RestrictedWord::class.java))).thenReturn(savedWord)
        val result = restrictedWordService.addWord(word)

        assertNotNull(result)
        assertEquals(normalizedWord, result?.word)
        verify(restrictedWordRepository).save(any(RestrictedWord::class.java))
    }

    @Test
    fun `should not add word when it already exists`() {
        val word = "existingword"
        val existingWord = RestrictedWord().apply { this.word = word }
        `when`(restrictedWordRepository.findByWord(word)).thenReturn(existingWord)
        val result = restrictedWordService.addWord(word)

        assertNull(result)
        verify(restrictedWordRepository, never()).save(any(RestrictedWord::class.java))
    }

    @Test
    fun `addWord should throw RestrictedWordException when repository throws exception`() {
        val word = "newword"
        val exception = RuntimeException("Database error")
        `when`(restrictedWordRepository.findByWord(word)).thenReturn(null)
        `when`(restrictedWordRepository.save(any(RestrictedWord::class.java))).thenThrow(exception)
        val thrownException = assertThrows<RestrictedWordException> {
            restrictedWordService.addWord(word)
        }

        assertEquals("Error adding word: $word", thrownException.message)
        assertEquals(exception, thrownException.cause)
    }

    @Test
    fun `should update word when it exists`() {
        val oldWord = "OldWord"
        val newWord = "NewWord"
        val normalizedOldWord = oldWord.lowercase()
        val normalizedNewWord = newWord.lowercase()
        val existingWord = RestrictedWord().apply { this.word = normalizedOldWord }
        `when`(restrictedWordRepository.findByWord(normalizedOldWord)).thenReturn(existingWord)
        val updatedWord = RestrictedWord().apply { this.word = normalizedNewWord }
        `when`(restrictedWordRepository.save(existingWord)).thenReturn(updatedWord)
        val result = restrictedWordService.updateWord(oldWord, newWord)

        assertNotNull(result)
        assertEquals(normalizedNewWord, result?.word)
        verify(restrictedWordRepository).save(existingWord)
    }

    @Test
    fun `should return null when trying to update non-existing word`() {
        val oldWord = "nonexistingword"
        val newWord = "newword"
        `when`(restrictedWordRepository.findByWord(oldWord)).thenReturn(null)
        val result = restrictedWordService.updateWord(oldWord, newWord)

        assertNull(result)
        verify(restrictedWordRepository, never()).save(any(RestrictedWord::class.java))
    }

    @Test
    fun `updateWord should throw RestrictedWordException when repository throws exception`() {
        val oldWord = "oldword"
        val newWord = "newword"
        val existingWord = RestrictedWord().apply { this.word = oldWord }
        val exception = RuntimeException("Database error")
        `when`(restrictedWordRepository.findByWord(oldWord)).thenReturn(existingWord)
        `when`(restrictedWordRepository.save(existingWord)).thenThrow(exception)
        val thrownException = assertThrows<RestrictedWordException> {
            restrictedWordService.updateWord(oldWord, newWord)
        }

        assertEquals("Error updating word from $oldWord to $newWord", thrownException.message)
        assertEquals(exception, thrownException.cause)
    }

    @Test
    fun `should delete word when it exists`() {
        val word = "wordtodelete"
        val existingWord = RestrictedWord().apply { this.word = word }
        `when`(restrictedWordRepository.findByWord(word)).thenReturn(existingWord)
        val result = restrictedWordService.deleteWord(word)

        assertTrue(result)
        verify(restrictedWordRepository).delete(existingWord)
    }

    @Test
    fun `should return false when trying to delete non-existing word`() {
        val word = "nonexistingword"
        `when`(restrictedWordRepository.findByWord(word)).thenReturn(null)
        val result = restrictedWordService.deleteWord(word)

        assertFalse(result)
        verify(restrictedWordRepository, never()).delete(any(RestrictedWord::class.java))
    }

    @Test
    fun `deleteWord should throw RestrictedWordException when repository throws exception`() {
        val word = "wordtodelete"
        val existingWord = RestrictedWord().apply { this.word = word }
        val exception = RuntimeException("Database error")
        `when`(restrictedWordRepository.findByWord(word)).thenReturn(existingWord)
        doThrow(exception).`when`(restrictedWordRepository).delete(existingWord)
        val thrownException = assertThrows<RestrictedWordException> {
            restrictedWordService.deleteWord(word)
        }

        assertEquals("Error deleting word: $word", thrownException.message)
        assertEquals(exception, thrownException.cause)
    }

    @Test
    fun `should handle empty word in addWord`() {
        val word = ""
        `when`(restrictedWordRepository.findByWord(word)).thenReturn(null)
        val savedWord = RestrictedWord().apply { this.word = word }
        `when`(restrictedWordRepository.save(any(RestrictedWord::class.java))).thenReturn(savedWord)
        val result = restrictedWordService.addWord(word)

        assertNotNull(result)
        assertEquals(word, result?.word)
        verify(restrictedWordRepository).save(any(RestrictedWord::class.java))
    }

    @Test
    fun `should handle special characters in words`() {
        val word = "wørd-with-spec!@l"
        `when`(restrictedWordRepository.findByWord(word)).thenReturn(null)
        val savedWord = RestrictedWord().apply { this.word = word }
        `when`(restrictedWordRepository.save(any(RestrictedWord::class.java))).thenReturn(savedWord)
        val result = restrictedWordService.addWord(word)

        assertNotNull(result)
        assertEquals(word, result?.word)
        verify(restrictedWordRepository).save(any(RestrictedWord::class.java))
    }

    @Test
    fun `should handle unicode characters in words`() {
        val word = "文字"
        `when`(restrictedWordRepository.findByWord(word)).thenReturn(null)
        val savedWord = RestrictedWord().apply { this.word = word }
        `when`(restrictedWordRepository.save(any(RestrictedWord::class.java))).thenReturn(savedWord)
        val result = restrictedWordService.addWord(word)

        assertNotNull(result)
        assertEquals(word, result?.word)
        verify(restrictedWordRepository).save(any(RestrictedWord::class.java))
    }

    @Test
    fun `should handle case-insensitive word addition`() {
        val word = "BadWord"
        val normalizedWord = word.lowercase()
        val existingWord = RestrictedWord().apply { this.word = normalizedWord }
        `when`(restrictedWordRepository.findByWord(normalizedWord)).thenReturn(existingWord)
        val result = restrictedWordService.addWord(word)

        assertNull(result)
        verify(restrictedWordRepository, never()).save(any(RestrictedWord::class.java))
    }

    @Test
    fun `should handle DataAccessException in repository`() {
        val exception = mock(DataAccessException::class.java)
        `when`(restrictedWordRepository.findAll()).thenThrow(exception)
        val thrownException = assertThrows<RestrictedWordException> {
            restrictedWordService.getWords()
        }

        assertEquals("Error fetching words", thrownException.message)
        assertEquals(exception, thrownException.cause)
    }
}
