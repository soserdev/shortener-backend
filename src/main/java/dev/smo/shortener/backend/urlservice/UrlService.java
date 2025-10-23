package dev.smo.shortener.backend.urlservice;

import dev.smo.shortener.backend.generator.KeyGeneratorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class UrlService {

    private final RestClient restClient;


    public UrlService(RestClient.Builder builder,
                      @Value("${shortener.urlservice.host:localhost}") String host,
                      @Value("${shortener.urlservice.port:8082}") int port) {
        var baseUrl = String.format("http://%s:%s", host, port);
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public UrlResponse save(UrlRequest urlRequest) {
        UrlResponse response = restClient.post()
                .uri("/api/v1/urlservice")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(urlRequest)
                .retrieve()
                .body(UrlResponse.class);
        return response;
    }

    public UrlResponse get(String shortUrl) {
        UrlResponse response = restClient.get()
                .uri("/api/v1/urlservice/" + shortUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(UrlResponse.class);
        return response;
    }
}
