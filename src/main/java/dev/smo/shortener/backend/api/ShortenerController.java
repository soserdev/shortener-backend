package dev.smo.shortener.backend.api;

import dev.smo.shortener.backend.generator.KeyGeneratorService;
import dev.smo.shortener.backend.urlservice.UrlRequest;
import dev.smo.shortener.backend.urlservice.UrlService;
import dev.smo.shortener.backend.util.URLValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.InvalidUrlException;

@CrossOrigin(origins = {"http://localhost:5175", "http://127.0.0.1"})
@RestController()
@Slf4j
public class ShortenerController {

    private final KeyGeneratorService keyGeneratorService;
    private final UrlService urlService;


    public ShortenerController(KeyGeneratorService keyGeneratorService, UrlService urlService) {
        this.keyGeneratorService = keyGeneratorService;
        this.urlService = urlService;
    }

    @CrossOrigin
    @PostMapping("/shorturl")
    public ResponseEntity<ResponseUrl> create(@RequestBody RequestUrl requestUrl) {
        if (!URLValidator.isValidURL(requestUrl.url())) {
            throw new InvalidUrlException("Invalid URL");
        }
        var nextKey = keyGeneratorService.getNextKey();

        var shortUrl = nextKey.key();
        var longUrl = requestUrl.url();
        var userId = "guest";

        var urlRequest = new UrlRequest(shortUrl, longUrl, userId);
        var urlResponse =  urlService.save(urlRequest);

        var responseUrl = new ResponseUrl(urlResponse.id(), longUrl, shortUrl);
        return new ResponseEntity<>(responseUrl, HttpStatus.CREATED);
    }

    @GetMapping("/shorturl/{shortUrl:[a-zA-Z0-9]{3,6}}")
    public ResponseEntity<ResponseUrl> getUrl(@PathVariable("shortUrl") String shortUrl) {
        var url = urlService.get(shortUrl);
        var responseUrl = new ResponseUrl(url.id(), url.longUrl(), url.shortUrl());
        log.info("Shortener: GET " + shortUrl + " -> " + url);
        return new ResponseEntity<>(responseUrl, HttpStatus.OK);
    }

}
