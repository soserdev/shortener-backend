package dev.smo.shortener.backend.urlservice;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UrlRequest(
        String shortUrl,
        String longUrl,
        String user,
        String status) {
}

