package dev.smo.shortener.backend.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CachedUrl(
        String id,
        String url,
        @JsonProperty("shorturl")
        String shortUrl
) {
}
