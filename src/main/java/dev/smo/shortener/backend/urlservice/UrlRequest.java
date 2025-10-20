package dev.smo.shortener.backend.urlservice;

public record UrlRequest(String shortUrl, String longUrl, String userid) {
}

