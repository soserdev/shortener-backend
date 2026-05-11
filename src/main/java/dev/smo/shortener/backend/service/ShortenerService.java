package dev.smo.shortener.backend.service;

import dev.smo.shortener.backend.cache.ShortUrlCache;
import dev.smo.shortener.backend.generator.KeyGeneratorService;
import dev.smo.shortener.backend.urlservice.PageResponse;
import dev.smo.shortener.backend.urlservice.UrlResponse;
import dev.smo.shortener.backend.urlservice.UrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShortenerService {

    private final KeyGeneratorService keyGeneratorService;
    private final UrlService urlService;
    private final ShortUrlCache shortUrlCache;

    public ShortenerService(
            KeyGeneratorService keyGeneratorService,
            UrlService urlService,
            ShortUrlCache shortUrlCache
    ) {
        this.keyGeneratorService = keyGeneratorService;
        this.urlService = urlService;
        this.shortUrlCache = shortUrlCache;
    }

    public UrlResponse create(String longUrl, String user) {

        var shortUrl = generateNewShortUrl();

        var urlResponse = urlService.save(shortUrl, longUrl, user);
        shortUrlCache.setCachedUrl(urlResponse.id(), shortUrl, longUrl);

        log.info("CREATE - USER: {} SHORTURL: {}", user, urlResponse);

        return urlResponse;
    }

    public UrlResponse update(String id, String status, String user) {

        var urlResponse = urlService.update(id, user, status);
        shortUrlCache.removeCachedUrl(urlResponse.shortUrl());

        log.info("UPDATE - USER: {} SHORTURL: {}", user, urlResponse);

        return urlResponse;
    }
    public PageResponse<UrlResponse> findAll(
            Authentication authentication,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        String user = authentication != null ? authentication.getName() : "default";

        log.info("GET ALL URLS - USER: {}", user);

        return urlService.findAll(user, page, size, sortBy, direction);
    }

    public UrlResponse get(String shortUrl) {
        var url = urlService.get(shortUrl);
        log.info("GET {} -> {}", shortUrl, url);
        return url;
    }

    private String generateNewShortUrl() {
        var nextKey = keyGeneratorService.getNextKey();
        return nextKey.key();
    }
}