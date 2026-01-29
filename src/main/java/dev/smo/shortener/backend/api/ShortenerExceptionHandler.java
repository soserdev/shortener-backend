package dev.smo.shortener.backend.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.InvalidUrlException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ShortenerExceptionHandler {

    @ExceptionHandler({HttpClientErrorException.NotFound.class})
    public ResponseEntity<Object> handleUrlNotFound(Exception ex, WebRequest request) {
        var apiError = new ShortenerApiError(String.valueOf(HttpStatus.NOT_FOUND.value()), "Not Found", "The provided URL is not found");
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({InvalidUrlException.class})
    public ResponseEntity<Object> handleInvalidUrl(Exception ex, WebRequest request) {
        var apiError = new ShortenerApiError(String.valueOf(HttpStatus.BAD_REQUEST.value()), "Bad Request", "The provided URL is not acceptable");
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        // Handle RequestUrl parameters not valid
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        log.error("Url validation error: ", errors);
        var apiError = new ShortenerApiError(String.valueOf(HttpStatus.BAD_REQUEST.value()), "Bad Request", "The URL validation failed");
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        log.error(ex.toString(), ex);
        ShortenerApiError apiError = new ShortenerApiError(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "Internal error", "Internal server error occurred");
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}