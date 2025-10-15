package dev.smo.shortener.backend.api;

import lombok.Data;

@Data
public class ShortenerApiError {

    private String status;
    private String message;
    private String description;

    public ShortenerApiError(String status, String message, String error) {
        this.status = status;
        this.message = message;
        this.description = error;
    }

}
