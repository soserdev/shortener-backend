package dev.smo.shortener.backend.api;

import dev.smo.shortener.backend.blacklist.BlacklistService;
import dev.smo.shortener.backend.cache.ShortUrlCache;
import dev.smo.shortener.backend.generator.KeyGeneratorService;
import dev.smo.shortener.backend.urlservice.UrlResponse;
import dev.smo.shortener.backend.urlservice.UrlService;
import dev.smo.shortener.backend.util.UrlUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.InvalidUrlException;

import java.util.List;

@Slf4j
@RestController()
public class ShortenerController {

    @Value("${app.host}")
    private String host;

    private final KeyGeneratorService keyGeneratorService;
    private final UrlService urlService;
    private final BlacklistService blacklistService;
    private final ShortUrlCache shortUrlCache;

    public ShortenerController(KeyGeneratorService keyGeneratorService, UrlService urlService,
                               BlacklistService blacklistService, ShortUrlCache shortUrlCache) {
        this.keyGeneratorService = keyGeneratorService;
        this.urlService = urlService;
        this.blacklistService = blacklistService;
        this.shortUrlCache = shortUrlCache;
    }

    @PostMapping("/shorturl")
    public ResponseEntity<ResponseUrl> create(@Valid @RequestBody RequestUrl requestUrl,
                                              Authentication authentication,
                                              @RequestHeader(value = "X-Forwarded-User",required = false) String forwardedUser) {
        if (!UrlUtils.isValidURL(requestUrl.url())) {
            throw new InvalidUrlException("Invalid URL");
        }
        // check for blacklisted hosts like 'localhost'
        var host = UrlUtils.extractHost(requestUrl.url());
        if (blacklistService.containsBlacklistedWord(host)) {
            throw new InvalidUrlException("Invalid URL");
        }

        var user = "default";
        // check for forwarded user which is set by traefik basic auth...
        if (forwardedUser != null) {
            user = forwardedUser;
        }
        // check for authenticated user and replace with his name...
        if (authentication != null) {
            user = authentication.getName();
            log.info("USER NAME: " + user);
        }

        var shortUrl = generateNewShortUrl();
        var longUrl = requestUrl.url();

        var urlResponse =  urlService.save(shortUrl, longUrl, user);

        shortUrlCache.setCachedUrl(urlResponse.id(), shortUrl, longUrl);

        var responseUrl = mapToResponseUrl(urlResponse);
        log.info("CREATE - USER: " + user + " SHORTURL: " + responseUrl);

        return new ResponseEntity<>(responseUrl, HttpStatus.CREATED);
    }

    // generate a new short url in the form "snib.me/1fa"...
    private String generateNewShortUrl() {
        var nextKey = keyGeneratorService.getNextKey();
        return host + "/" + nextKey.key();
    }

    @GetMapping("/shorturl")
    public ResponseEntity<List<ResponseUrl>> getUrls(
            Authentication authentication) {

        if (authentication == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        var    user = authentication.getName();

        log.info("GET ALL URLS - USER: " + user);
        return ResponseEntity.ok(urlService.findAll(user)
                .stream().map(this::mapToResponseUrl).toList());
    }


    @GetMapping("/shorturl/{shortUrl:[a-zA-Z0-9]{3,6}}")
    public ResponseEntity<ResponseUrl> getUrl(@PathVariable("shortUrl") String shortUrl) {

        var url = urlService.get(shortUrl);
        log.info("GET " + shortUrl + " -> " + url);
        return ResponseEntity.ok(mapToResponseUrl(url));
    }

    @GetMapping("/{shortUrlPath:[a-zA-Z0-9]{3,6}}")
    public ResponseEntity<Void> redirect(@PathVariable("shortUrlPath") String shortUrlPath) {

        final String url;
        var shortUrl = host + "/" + shortUrlPath;
        var cachedUrl = shortUrlCache.getCachedUrl(shortUrl);
        if (cachedUrl != null) {
            url = cachedUrl.url();
        } else {
            var retrievedUrl = urlService.get(shortUrl);
            // put the url in the cache
            shortUrlCache.setCachedUrl(retrievedUrl.id(), retrievedUrl.shortUrl(), retrievedUrl.longUrl());
            url = retrievedUrl.longUrl();
        }
        log.info("REDIRECT: " + shortUrl + " -> " + url);
        return ResponseEntity
                .status(HttpStatus.FOUND) // .status(HttpStatus.MOVED_PERMANENTLY)
                .header(HttpHeaders.LOCATION, url)
                .build();

    }

    public ResponseUrl mapToResponseUrl(UrlResponse url) {
        return new ResponseUrl(
                url.id(),
                url.longUrl(),
                url.shortUrl(),
                url.user(),
                url.created(),
                url.updated()
        );
    }
}
