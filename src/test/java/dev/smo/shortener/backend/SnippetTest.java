package dev.smo.shortener.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SnippetTest {

    @Test
    void testIt() {
        var host = "http://localhost:8080/actuator/health";
        var baseUrl = host.replaceAll("^(https?://[^/]+).*", "$1");
        assertEquals("http://localhost:8080", baseUrl);

        var path = host.replaceAll("^https?://[^/]+(.*)", "$1");
        assertEquals("/actuator/health", path);

    }
}
