package dev.smo.shortener.backend.api;

import dev.smo.shortener.backend.generator.KeyGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"http://localhost:5175", "http://127.0.0.1"})
@RestController()
@Slf4j
public class ShortenerController {

    private final KeyGeneratorService keyGeneratorService;

    public ShortenerController(KeyGeneratorService keyGeneratorService) {
        this.keyGeneratorService = keyGeneratorService;
    }

    @CrossOrigin
    @PostMapping("/shorturl")
    public ResponseEntity<ResponseUrl> create(@RequestBody RequestUrl requestUrl) {
        var nextKey = keyGeneratorService.getNextKey();
        var responseUrl = new ResponseUrl(nextKey.id(), requestUrl.url(), nextKey.key());
        return new ResponseEntity<>(responseUrl, HttpStatus.OK);
    }

}
