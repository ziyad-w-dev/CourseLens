package com.ziyad.courselens.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void fileTooLarge_isBadRequestNotServerError() {
        ResponseEntity<ErrorResponse> response =
                handler.handleFileTooLarge(new MaxUploadSizeExceededException(20L * 1024 * 1024));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("File is larger than 20 MB", response.getBody().getMessage());
    }
}
