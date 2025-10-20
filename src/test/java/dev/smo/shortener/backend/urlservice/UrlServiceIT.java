package dev.smo.shortener.backend.urlservice;

import dev.smo.shortener.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class UrlServiceIT {

    @Autowired
    UrlService urlService;

    @Test
    void testSave() {
        String shortUrl = UUID.randomUUID().toString().substring(0,7);
        String longUrl = "http://example.com";
        String userId = "007";
        var urlRequest = new UrlRequest(shortUrl, longUrl, userId);
        var urlResponse =  urlService.save(urlRequest);
        assertThat(urlResponse).isNotNull();
        assertThat(urlResponse.id()).isNotNull();
        assertEquals(shortUrl, urlResponse.shortUrl());
        assertEquals(longUrl, urlResponse.longUrl());
        assertEquals(userId, urlResponse.userid());
        assertThat(urlResponse.created()).isNotNull();
        assertThat(urlResponse.updated()).isNotNull();
    }
}