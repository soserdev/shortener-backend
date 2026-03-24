package dev.smo.shortener.backend.api;

import java.time.LocalDateTime;

public record ResponseUrl (

        String id,
        String url,
        String shortUrl,
        String user,
        LocalDateTime created,
        LocalDateTime updated) {

}
