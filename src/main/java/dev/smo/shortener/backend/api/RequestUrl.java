package dev.smo.shortener.backend.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RequestUrl(

        @Size(max = 128, message = "URL must not exceed 128 characters")
        String id,

        @Size(max = 2048, message = "URL must not exceed 2048 characters")
        String url,

        @Size(max = 128, message = "Short URL must not exceed 128 characters")
        String shortUrl,

        @Size(max = 16, message = "StatusL must not exceed 16 characters")
        String status

) {
}
