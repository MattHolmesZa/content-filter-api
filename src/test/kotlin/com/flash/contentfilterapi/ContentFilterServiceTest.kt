package com.flash.contentfilterapi

import com.flash.contentfilterapi.service.ContentFilterException
import com.flash.contentfilterapi.service.ContentFilterService
import com.flash.contentfilterapi.service.RestrictedWordService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ContentFilterServiceTest {

    private val restrictedWordService = mock(RestrictedWordService::class.java)
    private val contentFilterService = ContentFilterService(restrictedWordService)

    @Test
    fun `should replace restricted words with asterisks`() {
        `when`(restrictedWordService.getWords()).thenReturn(setOf("badword", "anotherbadword"))
        val input = "This is a badword and anotherbadword."
        val expected = "This is a ******* and **************."
        val result = contentFilterService.filterContent(input)

        assertEquals(expected, result)
    }

    @Test
    fun `should return input unchanged when there are no restricted words`() {
        `when`(restrictedWordService.getWords()).thenReturn(setOf())
        val input = "This is a clean sentence."
        val expected = "This is a clean sentence."
        val result = contentFilterService.filterContent(input)

        assertEquals(expected, result)
    }

    @Test
    fun `should handle case-insensitive replacement`() {
        `when`(restrictedWordService.getWords()).thenReturn(setOf("BadWord"))
        val input = "This is a badword."
        val expected = "This is a *******."
        val result = contentFilterService.filterContent(input)

        assertEquals(expected, result)
    }

    @Test
    fun `should replace multiple occurrences of restricted words`() {
        `when`(restrictedWordService.getWords()).thenReturn(setOf("badword"))
        val input = "badword badword badword"
        val expected = "******* ******* *******"
        val result = contentFilterService.filterContent(input)

        assertEquals(expected, result)
    }

    @Test
    fun `should handle overlapping restricted words`() {
        `when`(restrictedWordService.getWords()).thenReturn(setOf("bad", "badword"))
        val input = "This is bad and badword."
        val expected = "This is *** and *******."
        val result = contentFilterService.filterContent(input)

        assertEquals(expected, result)
    }

    @Test
    fun `should handle special regex characters in restricted words`() {
        `when`(restrictedWordService.getWords()).thenReturn(setOf("b(a)d", "wor[d]"))
        val input = "This is b(a)d and wor[d]."
        val expected = "This is ***** and ******."
        val result = contentFilterService.filterContent(input)

        assertEquals(expected, result)
    }

    @Test
    fun `should handle empty input string`() {
        `when`(restrictedWordService.getWords()).thenReturn(setOf("badword"))
        val input = ""
        val expected = ""
        val result = contentFilterService.filterContent(input)

        assertEquals(expected, result)
    }

    @Test
    fun `should throw WordSanitizerServiceException when restrictedWordService fails`() {
        `when`(restrictedWordService.getWords()).thenThrow(ContentFilterException("An error occurred.", Exception()))
        val input = "This is a badword."
        val exception = assertThrows<ContentFilterException> {
            contentFilterService.filterContent(input)
        }

        assertEquals("Error sanitizing words", exception.message)
        assertTrue(exception.cause is RuntimeException)
        assertEquals("An error occurred.", exception.cause?.message)
    }

    @Test
    fun `should handle input with unicode characters`() {
        `when`(restrictedWordService.getWords()).thenReturn(setOf("bädword"))
        val input = "This is a bädword."
        val expected = "This is a *******."
        val result = contentFilterService.filterContent(input)

        assertEquals(expected, result)
    }
}
