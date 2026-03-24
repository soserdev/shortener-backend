package dev.smo.shortener.backend.api;

import dev.smo.shortener.backend.blacklist.BlacklistService;
import dev.smo.shortener.backend.cache.ShortUrlCache;
import dev.smo.shortener.backend.generator.KeyGeneratorService;
import dev.smo.shortener.backend.urlservice.UrlRequest;
import dev.smo.shortener.backend.urlservice.UrlResponse;
import dev.smo.shortener.backend.urlservice.UrlService;
import dev.smo.shortener.backend.util.UrlUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
        if (forwardedUser != null) {
            user = forwardedUser;
        }
        if (authentication != null) {
            user = authentication.getName();
            log.info("USER NAME: " + user);
        }

        var nextKey = keyGeneratorService.getNextKey();

        var shortUrl = nextKey.key();
        var longUrl = requestUrl.url();

        var urlRequest = new UrlRequest(shortUrl, longUrl, user);
        var urlResponse =  urlService.save(urlRequest);

        shortUrlCache.setCachedUrl(urlResponse.id(), urlResponse.shortUrl(), urlResponse.longUrl(), urlResponse.user());

        var responseUrl = mapToResponseUrl(urlResponse);
        log.info("CREATE - USER: " + user + " SHORTURL: " + responseUrl);

        return new ResponseEntity<>(responseUrl, HttpStatus.CREATED);
    }

    @GetMapping("/shorturl/{shortUrl:[a-zA-Z0-9]{3,6}}")
    public ResponseEntity<ResponseUrl> getUrl(@PathVariable("shortUrl") String shortUrl) {

        var url = urlService.get(shortUrl);
        log.info("GET " + shortUrl + " -> " + url);
        return ResponseEntity.ok(mapToResponseUrl(url));
    }

    @GetMapping("/{shortUrlPath:[a-zA-Z0-9]{3,6}}")
    public ResponseEntity<Void> redirect(@PathVariable("shortUrlPath") String shortUrl){
        var userId = "default";
        final String url;
        var cachedUrl = shortUrlCache.getCachedUrl(shortUrl, userId);
        if (cachedUrl != null) {
            url = cachedUrl.url();
        } else {
            var retrievedUrl = urlService.get(shortUrl);
            // put the url in the cache
            shortUrlCache.setCachedUrl(retrievedUrl.id(), retrievedUrl.shortUrl(), retrievedUrl.longUrl(), retrievedUrl.user());
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
