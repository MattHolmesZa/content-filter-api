package com.flash.contentfilterapi.controller

import com.flash.contentfilterapi.service.ContentFilterService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Word Sanitizer", description = "API for sanitizing restricted words in input text")
@RestController
@RequestMapping("/api")
class ContentFilterController(private val contentFilterService: ContentFilterService) {

    @Operation(
        summary = "Sanitize input word",
        description = "Sanitizes the input replacing any restricted words with asterisks",
        parameters = [Parameter(name = "word", description = "Word or phrase to sanitize", required = true)],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully sanitized the input",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid input provided")
        ]
    )
    @GetMapping("/sanitize", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sanitize(@RequestParam word: String): ResponseEntity<String> {
        val sanitizedInput = contentFilterService.filterContent(word)
        return ResponseEntity.ok(sanitizedInput)
    }
}