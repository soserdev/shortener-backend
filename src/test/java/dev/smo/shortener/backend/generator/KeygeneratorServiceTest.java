package dev.smo.shortener.backend.generator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// Use this test for dev if service is up and running in docker compose
@Disabled
@SpringBootTest
public class KeygeneratorServiceTest {

    @Autowired
    KeyGeneratorService keyGeneratorService;

//    @Test
    void getNextKey() {
        KeyGeneratorResponse nextKey = keyGeneratorService.getNextKey();
        assertNotNull(nextKey, "Next key should not be null!");
        assertNotNull(nextKey.id());
        assertNotNull(nextKey.key());
    }
}
