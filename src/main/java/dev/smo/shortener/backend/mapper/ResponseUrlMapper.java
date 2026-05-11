package dev.smo.shortener.backend.mapper;

import dev.smo.shortener.backend.api.ResponseUrl;
import dev.smo.shortener.backend.urlservice.UrlResponse;
import org.springframework.stereotype.Component;

@Component
public class ResponseUrlMapper {

    public ResponseUrl toResponse(UrlResponse url) {
        return new ResponseUrl(
                url.id(),
                url.longUrl(),
                url.shortUrl(),
                url.user(),
                url.status(),
                url.created(),
                url.updated()
        );
    }
}