package dev.smo.shortener.backend.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class URLValidatorTest {

    @Test
    public void testValidHttpURL() {
        assertTrue(URLValidator.isValidURL("http://example.com"));
    }

    @Test
    public void testValidHttpsURL() {
        assertTrue(URLValidator.isValidURL("https://example.com"));
    }

    @Test
    public void testValidURLWithPath() {
        assertTrue(URLValidator.isValidURL("https://example.com/path/to/resource"));
    }

    @Test
    public void testValidURLWithQuery() {
        assertTrue(URLValidator.isValidURL("https://example.com/search?q=java"));
    }

    @Test
    public void testValidFTPURL() {
        assertTrue(URLValidator.isValidURL("ftp://example.com/resource.txt"));
    }

    @Test
    public void testInvalidURLNoProtocol() {
        assertFalse(URLValidator.isValidURL("example.com"));
    }

    @Test
    public void testInvalidURLBadFormat() {
        assertFalse(URLValidator.isValidURL("http:/example.com"));
    }

    @Test
    public void testInvalidURLSpaces() {
        assertFalse(URLValidator.isValidURL("https://exa mple.com"));
    }

    @Test
    public void testNullURL() {
        assertFalse(URLValidator.isValidURL(null));
    }

    @Test
    public void testEmptyURL() {
        assertFalse(URLValidator.isValidURL(""));
    }

    @Test
    public void testURLWithPort() {
        assertTrue(URLValidator.isValidURL("https://example.com:8080/path"));
    }

    @Test
    public void testURLWithSubdomain() {
        assertTrue(URLValidator.isValidURL("https://sub.domain.example.com"));
    }

    @Test
    public void testURLWithFragment() {
        assertTrue(URLValidator.isValidURL("https://example.com/page#section"));
    }
}
