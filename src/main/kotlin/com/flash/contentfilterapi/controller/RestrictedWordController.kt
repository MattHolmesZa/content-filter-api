package com.flash.contentfilterapi.controller

import com.flash.contentfilterapi.model.RestrictedWord
import com.flash.contentfilterapi.service.RestrictedWordService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class WordRequest(
    @field:NotBlank(message = "Word must not be blank")
    @field:Size(max = 255, message = "Word must be between 1 and 255 characters")
    val word: String
)

@Tag(name = "Restricted Word Management", description = "APIs for managing restricted words")
@RestController
@RequestMapping("/api/restricted-words")
class RestrictedWordController(private val restrictedWordService: RestrictedWordService) {

    @Operation(
        summary = "Get all restricted words",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "List of restricted words retrieved successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Set::class))]
            )
        ]
    )
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getWords(): ResponseEntity<Set<String>> {
        val words = restrictedWordService.getWords()
        return ResponseEntity.ok(words)
    }

    @Operation(
        summary = "Add a new restricted word",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Word to add",
            content = [Content(schema = Schema(implementation = WordRequest::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Restricted Word created successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = RestrictedWord::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request payload")
        ]
    )
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun addWord(@Valid @RequestBody wordRequest: WordRequest): ResponseEntity<RestrictedWord> {
        val word = restrictedWordService.addWord(wordRequest.word)
        return ResponseEntity.status(HttpStatus.CREATED).body(word)
    }

    @Operation(
        summary = "Update an existing restricted word",
        parameters = [Parameter(name = "word", description = "Existing word to update", required = true)],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "New word data",
            content = [Content(schema = Schema(implementation = WordRequest::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Word updated successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = RestrictedWord::class))]
            ),
            ApiResponse(responseCode = "404", description = "Word not found"),
            ApiResponse(responseCode = "400", description = "Invalid request payload")
        ]
    )
    @PutMapping("/{word}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateWord(
        @PathVariable word: String,
        @Valid @RequestBody newWordRequest: WordRequest
    ): ResponseEntity<RestrictedWord> {
        val updatedWord = restrictedWordService.updateWord(word, newWordRequest.word)
        return if (updatedWord != null) {
            ResponseEntity.ok(updatedWord)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    @Operation(
        summary = "Delete a restricted word",
        parameters = [Parameter(name = "word", description = "Word to delete", required = true)],
        responses = [
            ApiResponse(responseCode = "200", description = "Word deleted successfully"),
            ApiResponse(responseCode = "404", description = "Word not found")
        ]
    )
    @DeleteMapping("/{word}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun deleteWord(@PathVariable word: String): ResponseEntity<Void> {
        val deleted = restrictedWordService.deleteWord(word)
        return if (deleted) {
            ResponseEntity.status(HttpStatus.OK).build()
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
}