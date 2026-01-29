package dev.smo.shortener.backend.api;

import jakarta.validation.constraints.Size;

public record RequestUrl(

        @Size(max = 128, message = "URL must not exceed 128 characters")
        String id,

        @Size(max = 2048, message = "URL must not exceed 2048 characters")
        String url,

        @Size(max = 128, message = "Short URL must not exceed 128 characters")
        String shortUrl
    ) {
}
