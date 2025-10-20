package dev.smo.shortener.backend.urlservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@SpringBootTest
class UrlServiceTest {

    @Autowired
    UrlService urlService;

//    @Test
    void testSave() {
        String shortUrl = UUID.randomUUID().toString().substring(0,7);
        String longUrl = "http://example.com";
        String userId = "007";
        var urlRequest = new UrlRequest(shortUrl, longUrl, userId);
        var urlResponse =  urlService.save(urlRequest);
        assertNotNull(urlResponse);
        assertNotNull(urlResponse.id());
        assertNotNull(urlResponse.created());
        assertNotNull(urlResponse.updated());
        assertEquals(shortUrl, urlResponse.shortUrl());
        assertEquals(longUrl, urlResponse.longUrl());
        assertEquals(userId, urlResponse.userid());
    }
}