package dev.smo.shortener.backend.generator;

import dev.smo.shortener.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class KeyGeneratorServiceIT {

    @Autowired
    KeyGeneratorService keyGeneratorService;

    @Test
    void getNextKey() {
        KeyGeneratorResponse nextKey = keyGeneratorService.getNextKey();
        assertNotNull(nextKey, "Next key should not be null!");
        assertNotNull(nextKey.id());
        assertNotNull(nextKey.key());
    }

}