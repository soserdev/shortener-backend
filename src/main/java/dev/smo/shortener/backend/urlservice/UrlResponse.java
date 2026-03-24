package dev.smo.shortener.backend.urlservice;

import java.time.LocalDateTime;

public record UrlResponse(
        String id,
        String shortUrl,
        String longUrl,
        String user,
        LocalDateTime created,
        LocalDateTime updated) {
}
