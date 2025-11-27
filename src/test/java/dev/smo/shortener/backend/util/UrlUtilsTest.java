package dev.smo.shortener.backend.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UrlUtilsTest {

    // tests for isValidURL()
    @Test
    public void testValidHttpURL() {
        assertTrue(UrlUtils.isValidURL("http://example.com"));
    }

    @Test
    public void testValidHttpsURL() {
        assertTrue(UrlUtils.isValidURL("https://example.com"));
    }

    @Test
    public void testValidURLWithPath() {
        assertTrue(UrlUtils.isValidURL("https://example.com/path/to/resource"));
    }

    @Test
    public void testValidURLWithQuery() {
        assertTrue(UrlUtils.isValidURL("https://example.com/search?q=java"));
    }

    @Test
    public void testInvalidFTPURL() {
        assertFalse(UrlUtils.isValidURL("ftp://example.com/resource.txt"));
    }

    @Test
    public void testInvalidURLNoProtocol() {
        assertFalse(UrlUtils.isValidURL("example.com"));
    }

    @Test
    public void testInvalidURLBadFormat() {
        assertFalse(UrlUtils.isValidURL("http:/example.com"));
    }

    @Test
    public void testInvalidURLSpaces() {
        assertFalse(UrlUtils.isValidURL("https://exa mple.com"));
    }

    @Test
    public void testNullURL() {
        assertFalse(UrlUtils.isValidURL(null));
    }

    @Test
    public void testEmptyURL() {
        assertFalse(UrlUtils.isValidURL(""));
    }

    @Test
    public void testURLWithPort() {
        assertTrue(UrlUtils.isValidURL("https://example.com:8080/path"));
    }

    @Test
    public void testURLWithSubdomain() {
        assertTrue(UrlUtils.isValidURL("https://sub.domain.example.com"));
    }

    @Test
    public void testURLWithFragment() {
        assertTrue(UrlUtils.isValidURL("https://example.com/page#section"));
    }

    // tests for extractHost()
    @Test
    void testHttpUrl() {
        assertEquals("example.com",
                UrlUtils.extractHost("http://example.com"));
    }

    @Test
    void testHttpsUrl() {
        assertEquals("example.com",
                UrlUtils.extractHost("https://example.com/path"));
    }

    @Test
    void testUrlWithSubdomain() {
        assertEquals("sub.domain.org",
                UrlUtils.extractHost("https://sub.domain.org/anything/here"));
    }

    @Test
    void testUrlWithPort() {
        assertEquals("myserver.local",
                UrlUtils.extractHost("http://myserver.local:8080/api"));
    }

    @Test
    void testUrlWithQueryAndFragment() {
        assertEquals("example.com",
                UrlUtils.extractHost("https://example.com/path?x=1#section"));
    }

    @Test
    void testInvalidProtocol() {
        assertNull(UrlUtils.extractHost("ftp://example.com/resource"));
    }

    @Test
    void testMissingProtocol() {
        assertNull(UrlUtils.extractHost("example.com/path"));
    }

    @Test
    void testEmptyString() {
        assertNull(UrlUtils.extractHost(""));
    }

    @Test
    void testNullInput() {
        assertNull(UrlUtils.extractHost(null));
    }

}
