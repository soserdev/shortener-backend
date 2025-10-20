package dev.smo.shortener.backend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.smo.shortener.backend.TestcontainersConfiguration;
import dev.smo.shortener.backend.generator.KeyGeneratorResponse;
import dev.smo.shortener.backend.generator.KeyGeneratorService;
import dev.smo.shortener.backend.urlservice.UrlResponse;
import dev.smo.shortener.backend.urlservice.UrlService;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.Matchers.matchesPattern;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ShortenerControllerIT {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void testCreate() throws Exception {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var url = new JSONObject();
        url.put("url", "http://www.manning.com/books");

        ResponseEntity<UrlResponse> response = restTemplate.exchange("/shorturl", HttpMethod.POST, new HttpEntity<String>(url.toString(), headers), UrlResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    }
}