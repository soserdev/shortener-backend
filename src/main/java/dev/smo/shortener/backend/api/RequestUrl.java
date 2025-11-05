package dev.smo.shortener.backend.api;

public record RequestUrl(
        String id,
        String url,
        String shortUrl) {
}
