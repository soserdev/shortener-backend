package dev.smo.shortener.backend.api;

import dev.smo.shortener.backend.service.RedirectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(RedirectController.class)
class RedirectControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RedirectService redirectService;

    @Test
    void testRedirect() throws Exception {

        var shortUrl = "1fa";
        var url = "https://www.example.com/test";

        given(redirectService.resolve(shortUrl))
                .willReturn(url);

        mockMvc.perform(get("/" + shortUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())
                .andExpect(header().exists(LOCATION))
                .andExpect(header().string(LOCATION, containsString(url)));
    }

    @Test
    void testRedirectNotFound() throws Exception {

        var shortUrl = "1fa";

        given(redirectService.resolve(shortUrl))
                .willThrow(new UrlNotFoundException("The provided URL is not found"));

        mockMvc.perform(get("/" + shortUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound());
    }
}