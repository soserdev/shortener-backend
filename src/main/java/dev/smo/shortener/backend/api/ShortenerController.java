package dev.smo.shortener.backend.api;

import dev.smo.shortener.backend.blacklist.BlacklistService;
import dev.smo.shortener.backend.cache.ShortUrlCache;
import dev.smo.shortener.backend.generator.KeyGeneratorService;
import dev.smo.shortener.backend.urlservice.PageResponse;
import dev.smo.shortener.backend.urlservice.UrlResponse;
import dev.smo.shortener.backend.urlservice.UrlService;
import dev.smo.shortener.backend.util.UrlUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.InvalidUrlException;

import java.util.List;

@Slf4j
@Validated
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
        var hostToCheck = UrlUtils.extractHost(requestUrl.url());
        if (blacklistService.containsBlacklistedWord(hostToCheck)) {
            throw new InvalidUrlException("Invalid URL");
        }

        var user = resolveUser(authentication, forwardedUser);
        var shortUrl = generateNewShortUrl();
        var longUrl = requestUrl.url();

        var urlResponse =  urlService.save(shortUrl, longUrl, user);

        shortUrlCache.setCachedUrl(urlResponse.id(), shortUrl, longUrl);

        var responseUrl = mapToResponseUrl(urlResponse);
        log.info("CREATE - USER: " + user + " SHORTURL: " + responseUrl);

        return new ResponseEntity<>(responseUrl, HttpStatus.CREATED);
    }

    @PutMapping("/shorturl/id/{id}")
    public ResponseEntity<ResponseUrl> update(@PathVariable("id") @NotBlank(message = "id should not be null or empty")  String id,
                                              @Valid @RequestBody RequestUrl requestUrl,
                                              Authentication authentication) {
        var user = "default";

        // authentication should not be null since we configured that in the security config!
        user = authentication.getName();

        var status = requestUrl.status();

        var urlResponse = urlService.update(id, user, status);
        shortUrlCache.removeCachedUrl(urlResponse.shortUrl());

        var responseUrl = mapToResponseUrl(urlResponse);
        log.info("UPDATE - URL: " + user + " SHORTURL: " + responseUrl);

        return ResponseEntity.ok(responseUrl);
    }

    @GetMapping("/shorturl")
    public ResponseEntity<PageResponse<ResponseUrl>> getUrls(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {

        String user = (authentication != null) ? authentication.getName() : "default";

        log.info("GET ALL URLS - USER: {}", user);

        var result = urlService.findAll(user, page, size, sortBy, direction);

        var mapped = new PageResponse<>(
                result.content().stream()
                        .map(this::mapToResponseUrl)
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.first(),
                result.last(),
                result.numberOfElements(),
                result.empty()
        );

        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/shorturl/{shortUrl:[a-zA-Z0-9]{3,6}}")
    public ResponseEntity<ResponseUrl> getUrl(@PathVariable("shortUrl") String shortUrl) {

        var url = urlService.get(shortUrl);
        log.info("GET " + shortUrl + " -> " + url);
        return ResponseEntity.ok(mapToResponseUrl(url));
    }

    @GetMapping("/{shortUrlPath:[a-zA-Z0-9]{3,6}}")
    public ResponseEntity<Void> redirect(@PathVariable("shortUrlPath") String shortUrl) {

        final String url;
        log.info("REDIRECT    : " + shortUrl);
        var cachedUrl = shortUrlCache.getCachedUrl(shortUrl);
        log.debug("REDIRECT (2): CACHE URL: " + cachedUrl);
        if (cachedUrl != null) {
            url = cachedUrl.url();
        } else {
            var retrievedUrl = urlService.get(shortUrl);

            if (retrievedUrl != null && !"active".equals(retrievedUrl.status())) {
                throw new UrlNotFoundException(
                        "The provided URL is not found"
                );
            }
            log.debug("REDIRECT (3): " + retrievedUrl);
            // put the url in the cache
            shortUrlCache.setCachedUrl(retrievedUrl.id(), retrievedUrl.shortUrl(), retrievedUrl.longUrl());
            url = retrievedUrl.longUrl();
        }
        log.debug("REDIRECT (4): " + shortUrl + " -> " + url);
        return ResponseEntity
                .status(HttpStatus.FOUND) // .status(HttpStatus.MOVED_PERMANENTLY)
                .header(HttpHeaders.LOCATION, url)
                .build();

    }

    // generate a new short url in the form "snib.me/1fa"...
    private String generateNewShortUrl() {
        var nextKey = keyGeneratorService.getNextKey();
        return nextKey.key();
    }

    private String resolveUser(Authentication auth, String forwardedUser) {
        if (auth != null) return auth.getName();
        if (forwardedUser != null) return forwardedUser;
        return "default"; // ""anonymous";
    }

    private ResponseUrl mapToResponseUrl(UrlResponse url) {
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
