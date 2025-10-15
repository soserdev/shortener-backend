package dev.smo.shortener.backend.generator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class KeyGeneratorService {

    private final RestClient restClient;

    public KeyGeneratorService(RestClient.Builder builder,
                               @Value("${shortener.keygenerator.host:localhost}") String host,
                               @Value("${shortener.keygenerator.port:8082}") int port) {
        var baseUrl = String.format("http://%s:%s" , host, port);
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public KeyGeneratorResponse getNextKey() {
        KeyGeneratorResponse response = restClient.get()
                .uri("/api/keys/next")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(KeyGeneratorResponse.class);
        return response;
    }
}
