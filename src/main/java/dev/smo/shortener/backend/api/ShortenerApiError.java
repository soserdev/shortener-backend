package dev.smo.shortener.backend.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonPropertyOrder({ "timestamp", "status", "error", "message" })
public class ShortenerApiError {

    private LocalDateTime timestamp;
    private String status;
    private String error;
    private String message;

    public ShortenerApiError(String status, String error, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
    }

}
