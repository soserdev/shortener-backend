package dev.smo.shortener.backend.urlservice;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;

@Validated
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


    public UrlResponse save(String shortUrl, String longUrl, String user) {

        var urlRequest = new UrlRequest(shortUrl, longUrl, user, null);
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
    }

    public PageResponse<UrlResponse> findAll(
            String user,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/v1/urls")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .queryParam("sortBy", sortBy)
                            .queryParam("direction", direction);

                    if (user != null && !user.isBlank()) {
                        uriBuilder.queryParam("user", user);
                    }

                    return uriBuilder.build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<PageResponse<UrlResponse>>() {});
    }

    public UrlResponse update(@NotBlank(message = "id should not be null or empty") String id, String user, String status) {
        var urlRequest = new UrlRequest(null, null, user, status);
        return restClient.put()
                .uri("/api/v1/urls/" + id )
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(urlRequest)
                .retrieve()
                .body(UrlResponse.class);
    }
}
