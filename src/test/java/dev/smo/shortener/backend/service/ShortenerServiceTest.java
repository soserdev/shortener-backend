package dev.smo.shortener.backend.service;

import dev.smo.shortener.backend.cache.ShortUrlCache;
import dev.smo.shortener.backend.generator.KeyGeneratorResponse;
import dev.smo.shortener.backend.generator.KeyGeneratorService;
import dev.smo.shortener.backend.urlservice.PageResponse;
import dev.smo.shortener.backend.urlservice.UrlResponse;
import dev.smo.shortener.backend.urlservice.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

class ShortenerServiceTest {

    private KeyGeneratorService keyGeneratorService;
    private UrlService urlService;
    private ShortUrlCache shortUrlCache;

    private ShortenerService shortenerService;

    @BeforeEach
    void setUp() {
        keyGeneratorService = mock(KeyGeneratorService.class);
        urlService = mock(UrlService.class);
        shortUrlCache = mock(ShortUrlCache.class);

        shortenerService = new ShortenerService(
                keyGeneratorService,
                urlService,
                shortUrlCache
        );
    }

    @Test
    void testCreateShortUrl() {

        var longUrl = "https://www.example.com/test";
        var user = "default";
        var shortUrl = "1fa";

        var keyResponse = new KeyGeneratorResponse(4784L, shortUrl);

        given(keyGeneratorService.getNextKey()).willReturn(keyResponse);
        willDoNothing().given(shortUrlCache).setCachedUrl(any(), any(), any());

        var id = "012345670123456701234567";

        var urlResponse = new UrlResponse(
                id,
                shortUrl,
                longUrl,
                user,
                "active",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(urlService.save(shortUrl, longUrl, user))
                .willReturn(urlResponse);

        var result = shortenerService.create(longUrl, user);

        assertThat(result).isEqualTo(urlResponse);

        then(keyGeneratorService).should().getNextKey();
        then(urlService).should().save(shortUrl, longUrl, user);
        then(shortUrlCache).should().setCachedUrl(id, shortUrl, longUrl);
    }

    @Test
    void testUpdateShortUrl() {

        var id = "id-1";
        var status = "inactive";
        var user = "user-1";
        var shortUrl = "1fa";

        var response = new UrlResponse(
                id,
                shortUrl,
                "https://www.example.com/test",
                user,
                status,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(urlService.update(id, user, status)).willReturn(response);

        var result = shortenerService.update(id, status, user);

        assertThat(result).isEqualTo(response);

        then(urlService).should().update(id, user, status);
        then(shortUrlCache).should().removeCachedUrl(shortUrl);
    }



    @Test
    void testFindAll() {

        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("user-1");

        var content = List.of(
                new UrlResponse(
                        "id-1",
                        "1fa",
                        "https://www.example.com/test",
                        "user-1",
                        "active",
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );

        var page = new PageResponse<>(
                content,
                0,
                10,
                1,
                1,
                true,
                true,
                1,
                false
        );

        given(urlService.findAll("user-1", 0, 10, "created", "desc"))
                .willReturn(page);

        var result = shortenerService.findAll(auth, 0, 10, "created", "desc");

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).shortUrl()).isEqualTo("1fa");

        then(urlService).should().findAll("user-1", 0, 10, "created", "desc");
    }

    @Test
    void testGetShortUrl() {

        var shortUrl = "1fa";

        var response = new UrlResponse(
                "id-1",
                shortUrl,
                "https://www.example.com/test",
                "007",
                "active",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(urlService.get(shortUrl)).willReturn(response);

        var result = shortenerService.get(shortUrl);

        assertThat(result).isEqualTo(response);

        then(urlService).should().get(shortUrl);
    }
}