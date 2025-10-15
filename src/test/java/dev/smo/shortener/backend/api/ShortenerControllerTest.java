package dev.smo.shortener.backend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.smo.shortener.backend.generator.KeyGeneratorResponse;
import dev.smo.shortener.backend.generator.KeyGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(ShortenerController.class)
class ShortenerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    KeyGeneratorService keyGeneratorService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testCreate() throws Exception {
        var url = "https://www.manning.com/books/spring-in-action-sixth-edition";
        var id = 4784;
        var shortUrl = "1fa";
        var requestUrl = new RequestUrl(null, url, null);

        var keyGeneratorResponse = new KeyGeneratorResponse(id, shortUrl);
        given(keyGeneratorService.getNextKey()).willReturn(keyGeneratorResponse);

        mockMvc.perform(post("/shorturl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.url", is(url)))
                .andExpect(jsonPath("$.shortUrl", is(shortUrl)));
    }
}