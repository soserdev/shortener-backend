package dev.smo.shortener.backend.urlservice;

import java.time.LocalDateTime;

public record UrlResponse(
        String id,
        String shortUrl,
        String longUrl,
        String user,
        String status,
        LocalDateTime created,
        LocalDateTime updated) {
}
