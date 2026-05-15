package dev.smo.shortener.backend.api;

import dev.smo.shortener.backend.blacklist.BlacklistService;
import dev.smo.shortener.backend.mapper.PageResponseMapper;
import dev.smo.shortener.backend.mapper.ResponseUrlMapper;
import dev.smo.shortener.backend.service.ShortenerService;
import dev.smo.shortener.backend.urlservice.PageResponse;
import dev.smo.shortener.backend.util.UrlUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.InvalidUrlException;

@Validated
@RestController
@RequestMapping("/shorturl")
public class ShortenerController {

    private final BlacklistService blacklistService;
    private final ShortenerService shortenerService;
    private final ResponseUrlMapper responseUrlMapper;
    private final PageResponseMapper pageResponseMapper;

    public ShortenerController(
            BlacklistService blacklistService,
            ShortenerService shortenerService,
            ResponseUrlMapper responseUrlMapper,
            PageResponseMapper pageResponseMapper
    ) {
        this.blacklistService = blacklistService;
        this.shortenerService = shortenerService;
        this.responseUrlMapper = responseUrlMapper;
        this.pageResponseMapper = pageResponseMapper;
    }

    @PostMapping
    public ResponseEntity<ResponseUrl> create(
            @Valid @RequestBody RequestUrl requestUrl,
            Authentication authentication,
            @RequestHeader(value = "X-Forwarded-User", required = false) String forwardedUser
    ) {
        var longUrl = requestUrl.url();
        validateLongUrl(longUrl);

        var user = resolveUser(authentication, forwardedUser);

        var result = shortenerService.create(longUrl, user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseUrlMapper.toResponse(result));
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<ResponseUrl> update(
            @PathVariable("id") String id,
            @Valid @RequestBody RequestUrl requestUrl,
            Authentication authentication
    ) {
        var user = authentication.getName();

        var result = shortenerService.update(id, requestUrl.status(), user);

        return ResponseEntity.ok(
                responseUrlMapper.toResponse(result)
        );
    }

    @GetMapping
    public ResponseEntity<PageResponse<ResponseUrl>> getUrls(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        var result = shortenerService.findAll(authentication, page, size, sortBy, direction);

        return ResponseEntity.ok(pageResponseMapper.toResponse(result));
    }

    @GetMapping("/{shortUrl:[a-zA-Z0-9]{3,6}}")
    public ResponseEntity<ResponseUrl> getUrl(@PathVariable String shortUrl) {
        return ResponseEntity.ok(
                responseUrlMapper.toResponse(shortenerService.get(shortUrl))
        );
    }

    // ---------------- validation ----------------

    private void validateLongUrl(String longUrl) {
        if (!UrlUtils.isValidURL(longUrl)) {
            throw new InvalidUrlException("Invalid URL");
        }

        var host = UrlUtils.extractHost(longUrl);

        if (blacklistService.containsBlacklistedWord(host)) {
            throw new InvalidUrlException("Invalid URL");
        }
    }

    // ---------------- user resolution ----------------

    private String resolveUser(Authentication auth, String forwardedUser) {
        if (auth != null) return auth.getName();
        if (forwardedUser != null) return forwardedUser;
        return "default";
    }
}