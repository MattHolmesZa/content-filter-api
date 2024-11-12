package com.flash.contentfilterapi.config

import com.flash.contentfilterapi.service.ContentFilterException
import com.flash.contentfilterapi.service.RestrictedWordException
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

@RestControllerAdvice
class RestControllerExceptionHandler {

    private val logger = LoggerFactory.getLogger(RestControllerExceptionHandler::class.java)

    @ExceptionHandler(RestrictedWordException::class)
    @ApiResponses(
        ApiResponse(responseCode = "500", description = "Internal server error in Restricted Word Service")
    )
    fun handleWordServiceException(
        exception: RestrictedWordException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("RestrictedWordException: {}", exception.message)
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = exception.message ?: "An error occurred in the Restricted Word Service",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(ContentFilterException::class)
    @ApiResponses(
        ApiResponse(responseCode = "500", description = "Internal server error in Content Filter Service")
    )
    fun handleWordSanitizerServiceException(
        exception: ContentFilterException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("WordSanitizerServiceException: {}", exception.message)
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = exception.message ?: "An error occurred in the Content Filter Service",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ApiResponses(
        ApiResponse(responseCode = "400", description = "Invalid request payload")
    )
    fun handleInvalidRequestPayload(
        exception: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Invalid request payload: {}", exception.message)
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "Invalid request payload",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    @ApiResponses(
        ApiResponse(responseCode = "500", description = "Unhandled server error")
    )
    fun handleGenericException(
        exception: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception: {}", exception.message, exception)
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = "An unexpected error occurred.",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}