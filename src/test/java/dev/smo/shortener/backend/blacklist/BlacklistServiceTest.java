package dev.smo.shortener.backend.blacklist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlacklistServiceTest {

    private BlacklistService blacklistService;

    @BeforeEach
    void setUp() {
        BlacklistProperties properties = new BlacklistProperties();
        properties.setWords(List.of("spam", "scam", "illegal", "forbidden", "coffee colleague"));
        blacklistService = new BlacklistService(properties);
    }

    @Test
    void testContainsBlacklistedWord_WithBlacklistedWord() {
        String text = "This message contains a scam.";
        assertTrue(blacklistService.containsBlacklistedWord(text));
    }

    @Test
    void testContainsBlacklistedWord_WithBlacklistedPhrase() {
        String text = "I met my coffee colleague today.";
        assertTrue(blacklistService.containsBlacklistedWord(text));
    }

    @Test
    void testContainsBlacklistedWord_CaseInsensitive() {
        String text = "This is a SPAM message.";
        assertTrue(blacklistService.containsBlacklistedWord(text));
    }

    @Test
    void testContainsBlacklistedWord_NoBlacklistedWord() {
        String text = "Hello, how are you?";
        assertFalse(blacklistService.containsBlacklistedWord(text));
    }

    @Test
    void testContainsBlacklistedWord_NullOrEmptyText() {
        assertFalse(blacklistService.containsBlacklistedWord(null));
        assertFalse(blacklistService.containsBlacklistedWord(""));
        assertFalse(blacklistService.containsBlacklistedWord("   "));
    }
}


