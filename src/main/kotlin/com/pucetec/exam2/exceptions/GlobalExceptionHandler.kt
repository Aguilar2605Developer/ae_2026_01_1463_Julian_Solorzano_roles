package com.pucetec.exam2.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.message ?: "No encontrado", LocalDateTime.now()))
    }

    @ExceptionHandler(BusinessValidationException::class)
    fun handleBusinessValidation(ex: BusinessValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.message ?: "Error de validación", LocalDateTime.now()))
    }
}

data class ErrorResponse(val status: Int, val message: String, val timestamp: LocalDateTime)