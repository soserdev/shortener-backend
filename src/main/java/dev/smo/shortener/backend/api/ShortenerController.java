package dev.smo.shortener.backend.api;

import dev.smo.shortener.backend.blacklist.BlacklistService;
import dev.smo.shortener.backend.cache.ShortUrlCache;
import dev.smo.shortener.backend.generator.KeyGeneratorService;
import dev.smo.shortener.backend.urlservice.UrlRequest;
import dev.smo.shortener.backend.urlservice.UrlService;
import dev.smo.shortener.backend.util.URLValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.InvalidUrlException;

import java.net.URI;

@Slf4j
//@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:5175", "http://127.0.0.1"}) -- see CorsConfig
@RestController()
public class ShortenerController {

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
    public ResponseEntity<ResponseUrl> create(@RequestBody RequestUrl requestUrl) {
        if (!URLValidator.isValidURL(requestUrl.url())) {
            throw new InvalidUrlException("Invalid URL");
        }

        if (blacklistService.containsBlacklistedWord(requestUrl.url())) {
            throw new InvalidUrlException("Invalid URL");
        }

        var nextKey = keyGeneratorService.getNextKey();

        var shortUrl = nextKey.key();
        var longUrl = requestUrl.url();
        var userId = "default";

        var urlRequest = new UrlRequest(shortUrl, longUrl, userId);
        var urlResponse =  urlService.save(urlRequest);

        shortUrlCache.setCachedUrl(urlResponse.id(), urlResponse.shortUrl(), urlResponse.longUrl(), urlResponse.userid());

        var responseUrl = new ResponseUrl(urlResponse.id(), longUrl, shortUrl);
        return new ResponseEntity<>(responseUrl, HttpStatus.CREATED);
    }

    @GetMapping("/shorturl/{shortUrl:[a-zA-Z0-9]{3,6}}")
    public ResponseEntity<ResponseUrl> getUrl(@PathVariable("shortUrl") String shortUrl) {
        var userId = "default";
        var cachedUrl = shortUrlCache.getCachedUrl(shortUrl, userId);
        ResponseUrl responseUrl;
        if (cachedUrl != null) {
            responseUrl = new ResponseUrl(cachedUrl.id(), cachedUrl.url(), cachedUrl.shortUrl());
        } else {
            var url = urlService.get(shortUrl);
            responseUrl = new ResponseUrl(url.id(), url.longUrl(), url.shortUrl());
            shortUrlCache.setCachedUrl(url.id(), url.shortUrl(), url.longUrl(), url.userid());
            log.info("Shortener: GET " + shortUrl + " -> " + url);
        }
        return new ResponseEntity<>(responseUrl, HttpStatus.OK);
    }

    @GetMapping("/{shortUrlPath:[a-zA-Z0-9]{3,6}}")
    public ResponseEntity<Void> redirect(@PathVariable("shortUrlPath") String shortUrl){
        var userId = "default";
        final String url;

        var cachedUrl = shortUrlCache.getCachedUrl(shortUrl, userId);
        if (cachedUrl != null) {
            url = cachedUrl.url();
        } else {
            // todo: yes, urlservice needs a userid in the future so we can support users with an own domain!
            var retrievedUrl = urlService.get(shortUrl);
            // put the url in the cache
            shortUrlCache.setCachedUrl(retrievedUrl.id(), retrievedUrl.shortUrl(), retrievedUrl.longUrl(), retrievedUrl.userid());
            url = retrievedUrl.longUrl();
        }
        log.info("Shortener: Redirect " + shortUrl + " -> " + url);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }
}
