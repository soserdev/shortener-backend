package dev.smo.shortener.backend.service;

import dev.smo.shortener.backend.api.UrlNotFoundException;
import dev.smo.shortener.backend.cache.CachedUrl;
import dev.smo.shortener.backend.cache.ShortUrlCache;
import dev.smo.shortener.backend.urlservice.UrlResponse;
import dev.smo.shortener.backend.urlservice.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

class RedirectServiceTest {

    private ShortUrlCache shortUrlCache;
    private UrlService urlService;

    private RedirectService redirectService;

    @BeforeEach
    void setUp() {

        shortUrlCache = mock(ShortUrlCache.class);
        urlService = mock(UrlService.class);

        redirectService = new RedirectService(
                shortUrlCache,
                urlService
        );
    }

    @Test
    void resolve_shouldReturnCachedUrl_whenCacheHit() {

        var shortUrl = "1fa";
        var longUrl = "https://www.example.com/test";

        var cachedUrl = new CachedUrl(
                "id-1",
                longUrl,
                shortUrl
        );

        given(shortUrlCache.getCachedUrl(shortUrl))
                .willReturn(cachedUrl);

        var result = redirectService.resolve(shortUrl);

        assertThat(result).isEqualTo(longUrl);

        then(shortUrlCache).should().getCachedUrl(shortUrl);
        then(urlService).shouldHaveNoInteractions();
    }

    @Test
    void resolve_shouldLoadFromDatabase_whenCacheMiss() {

        var shortUrl = "1fa";
        var longUrl = "https://www.example.com/test";

        given(shortUrlCache.getCachedUrl(shortUrl))
                .willReturn(null);

        var response = new UrlResponse(
                "id-1",
                shortUrl,
                longUrl,
                "007",
                "active",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(urlService.get(shortUrl))
                .willReturn(response);

        var result = redirectService.resolve(shortUrl);

        assertThat(result).isEqualTo(longUrl);

        then(urlService).should().get(shortUrl);

        then(shortUrlCache).should().setCachedUrl(
                "id-1",
                shortUrl,
                longUrl
        );
    }

    @Test
    void resolve_shouldThrow_whenUrlInactive() {

        var shortUrl = "1fa";

        given(shortUrlCache.getCachedUrl(shortUrl))
                .willReturn(null);

        var response = new UrlResponse(
                "id-1",
                shortUrl,
                "https://www.example.com/test",
                "007",
                "inactive",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(urlService.get(shortUrl))
                .willReturn(response);

        assertThatThrownBy(() -> redirectService.resolve(shortUrl))
                .isInstanceOf(UrlNotFoundException.class)
                .hasMessage("The provided URL is not found");

        then(urlService).should().get(shortUrl);

        then(shortUrlCache).should(never())
                .setCachedUrl(any(), any(), any());
    }

    @Test
    void resolve_shouldThrow_whenUrlNotFound() {

        var shortUrl = "1fa";

        given(shortUrlCache.getCachedUrl(shortUrl))
                .willReturn(null);

        given(urlService.get(shortUrl))
                .willReturn(null);

        assertThatThrownBy(() -> redirectService.resolve(shortUrl))
                .isInstanceOf(UrlNotFoundException.class)
                .hasMessage("The provided URL is not found");

        then(urlService).should().get(shortUrl);

        then(shortUrlCache).should(never())
                .setCachedUrl(any(), any(), any());
    }
}