package dev.smo.shortener.backend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.smo.shortener.backend.blacklist.BlacklistService;
import dev.smo.shortener.backend.cache.ShortUrlCache;
import dev.smo.shortener.backend.generator.KeyGeneratorResponse;
import dev.smo.shortener.backend.generator.KeyGeneratorService;
import dev.smo.shortener.backend.urlservice.UrlResponse;
import dev.smo.shortener.backend.urlservice.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.http.HttpHeaders.LOCATION;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("test")
@WebMvcTest(ShortenerController.class)
class ShortenerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    KeyGeneratorService keyGeneratorService;

    @MockitoBean
    UrlService urlService;

    @MockitoBean
    BlacklistService blacklistService;

    @MockitoBean
    ShortUrlCache shortUrlCache;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testCreate() throws Exception {
        var url = "https://www.example.com/test";
        var shortUrl = "1fa";
        var requestUrl = new RequestUrl(null, url, null);
        var keyGeneratorResponse = new KeyGeneratorResponse(4784L, shortUrl);

        given(keyGeneratorService.getNextKey()).willReturn(keyGeneratorResponse);
        given(blacklistService.containsBlacklistedWord(any())).willReturn(false);
        willDoNothing().given(shortUrlCache).setCachedUrl(any(), any(), any(), any());

        var id = UUID.randomUUID().toString();
        given(urlService.save(any())).willReturn(new UrlResponse(id, shortUrl, url, "default", LocalDateTime.now(), LocalDateTime.now()));

        mockMvc.perform(post("/shorturl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUrl)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.url", is(url)))
                .andExpect(jsonPath("$.shortUrl", is(shortUrl)));
    }

    @Test
    void testCreateMalformedUrl() throws Exception {
        var url = "https://jlsfjlas.hh-heise.de/jsljflsjfl?kjsflfj=%s/";
        var requestUrl = new RequestUrl(null, url, null);

        mockMvc.perform(post("/shorturl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUrl)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUrlTooLong() throws Exception {
        var url = "https://example.com/" + "a".repeat(2050);
        var requestUrl = new RequestUrl(null, url, null);

        mockMvc.perform(post("/shorturl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUrl)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetShortUrl() throws Exception {
        var url = "https://www.example.com/test";
        var shortUrl = "1fa";
        var id = "012345670123456701234567";

        UrlResponse urlResponse = new UrlResponse(id, shortUrl, url, "007", null, null);
        given(urlService.get(any())).willReturn(urlResponse);

        mockMvc.perform(get("/shorturl/" + shortUrl)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.url", is(url)))
                .andExpect(jsonPath("$.shortUrl", is(shortUrl)));
    }

    @Test
    void testGetShortUrlNotFound() throws Exception {
        var shortUrl = "1fa";

        given(urlService.get(any())).willThrow(HttpClientErrorException.NotFound.class);

        mockMvc.perform(get("/shorturl/" + shortUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRedirect() throws Exception {
        var url = "https://www.example.com/test";
        var shortUrl = "1fa";
        var id = "012345670123456701234567";

        UrlResponse urlResponse = new UrlResponse(id, shortUrl, url, "007", null, null);
        given(urlService.get(any())).willReturn(urlResponse);

        mockMvc.perform(get("/" + shortUrl))
                .andExpect(status().isFound())
                .andExpect(header().exists(LOCATION))
                .andExpect(header().string(LOCATION, containsString(url)));
    }
}