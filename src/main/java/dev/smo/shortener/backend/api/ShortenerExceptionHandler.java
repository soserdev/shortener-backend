package dev.smo.shortener.backend.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.InvalidUrlException;

@Slf4j
@RestControllerAdvice
public class ShortenerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({InvalidUrlException.class})
    public ResponseEntity<Object> handleInvalidUrl(Exception ex, WebRequest request) {
        var apiError = new ShortenerApiError(String.valueOf(HttpStatus.BAD_REQUEST.value()), "Invalid Url", "The provided URL is not acceptable");
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        log.error(ex.toString(), ex);
        ShortenerApiError apiError = new ShortenerApiError(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "Internel server error", "Internal server error occurred");
        return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}