package dev.smo.shortener.backend.api;

import dev.smo.shortener.backend.blacklist.BlacklistService;
import dev.smo.shortener.backend.mapper.PageResponseMapper;
import dev.smo.shortener.backend.mapper.ResponseUrlMapper;
import dev.smo.shortener.backend.service.ShortenerService;
import dev.smo.shortener.backend.urlservice.UrlResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(ShortenerController.class)
@Import({ResponseUrlMapper.class, PageResponseMapper.class})
class ShortenerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ShortenerService shortenerService;

    @MockitoBean
    BlacklistService blacklistService;

    // ---------------- CREATE ----------------

    @Test
    void testCreate() throws Exception {

        var url = "https://www.example.com/test";
        var shortUrl = "1fa";
        var id = UUID.randomUUID().toString();

        var auth = new UsernamePasswordAuthenticationToken("default", null);

        var serviceResponse = new UrlResponse(
                id,
                shortUrl,
                url,
                "default",
                "active",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(shortenerService.create(eq(url), eq("default")))
                .thenReturn(serviceResponse);

        mockMvc.perform(post("/shorturl")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RequestUrl(null, url, null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.url", is(url)))
                .andExpect(jsonPath("$.shortUrl", is(shortUrl)));
    }

    // ---------------- MALFORMED ----------------

    @Test
    void testCreateMalformedUrl() throws Exception {

        var url = "https://jlsfjlas.hh-heise.de/jsljflsjfl?kjsflfj=%s/";

        var auth = new UsernamePasswordAuthenticationToken("default", null);

        when(shortenerService.create(eq(url), eq("default")))
                .thenThrow(new org.springframework.web.util.InvalidUrlException("Invalid URL"));

        mockMvc.perform(post("/shorturl")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RequestUrl(null, url, null, null))))
                .andExpect(status().isBadRequest());
    }

    // ---------------- TOO LONG ----------------

    @Test
    void testCreateUrlTooLong() throws Exception {

        var url = "https://example.com/" + "a".repeat(2050);

        mockMvc.perform(post("/shorturl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RequestUrl(null, url, null, null))))
                .andExpect(status().isBadRequest());
    }

    // ---------------- GET ----------------

    @Test
    void testGetShortUrl() throws Exception {

        var url = "https://www.example.com/test";
        var shortUrl = "1fa";
        var id = "012345670123456701234567";

        var serviceResponse = new UrlResponse(
                id,
                shortUrl,
                url,
                "007",
                null,
                null,
                null
        );

        when(shortenerService.get(shortUrl))
                .thenReturn(serviceResponse);

        mockMvc.perform(get("/shorturl/" + shortUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.url", is(url)))
                .andExpect(jsonPath("$.shortUrl", is(shortUrl)));
    }

    // ---------------- NOT FOUND ----------------

    @Test
    void testGetShortUrlNotFound() throws Exception {

        var shortUrl = "1fa";

        when(shortenerService.get(shortUrl))
                .thenThrow(new UrlNotFoundException("The provided URL is not found"));

        mockMvc.perform(get("/shorturl/" + shortUrl))
                .andExpect(status().isFound());
    }

    // ---------------- JSON helper ----------------

    private String json(RequestUrl requestUrl) {
        return """
                {
                  "url":"%s"
                }
                """.formatted(requestUrl.url());
    }
}