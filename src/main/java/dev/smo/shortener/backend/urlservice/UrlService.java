package dev.smo.shortener.backend.urlservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

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
        return restClient.post()
                .uri("/api/v1/urls")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(urlRequest)
                .retrieve()
                .body(UrlResponse.class);
    }

    public UrlResponse get(String shortUrl) {
        return restClient.get()
                .uri("/api/v1/urls/short/" + shortUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(UrlResponse.class);
        return response;
    }
}
