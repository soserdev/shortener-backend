package dev.smo.shortener.backend.service;

import dev.smo.shortener.backend.api.UrlNotFoundException;
import dev.smo.shortener.backend.cache.ShortUrlCache;
import dev.smo.shortener.backend.urlservice.UrlResponse;
import dev.smo.shortener.backend.urlservice.UrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedirectService {

    private final ShortUrlCache shortUrlCache;
    private final UrlService urlService;

    public RedirectService(
            ShortUrlCache shortUrlCache,
            UrlService urlService
    ) {
        this.shortUrlCache = shortUrlCache;
        this.urlService = urlService;
    }

    public String resolve(String shortUrl) {

        log.info("REDIRECT: {}", shortUrl);

        // ---------------- CACHE ----------------
        var cached = shortUrlCache.getCachedUrl(shortUrl);

        if (cached != null) {
            log.debug("CACHE HIT: {}", cached.url());
            return cached.url();
        }

        // ---------------- DB ----------------
        UrlResponse retrieved = urlService.get(shortUrl);

        if (retrieved == null || !"active".equals(retrieved.status())) {
            throw new UrlNotFoundException("The provided URL is not found");
        }

        log.debug("CACHE MISS -> DB: {}", retrieved);

        // ---------------- UPDATE CACHE ----------------
        shortUrlCache.setCachedUrl(
                retrieved.id(),
                retrieved.shortUrl(),
                retrieved.longUrl()
        );

        return retrieved.longUrl();
    }
}