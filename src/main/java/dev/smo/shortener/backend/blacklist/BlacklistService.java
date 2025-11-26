package dev.smo.shortener.backend.blacklist;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlacklistService {

    private final List<String> blacklistedWords;

    public BlacklistService(BlacklistProperties properties) {
        this.blacklistedWords = properties.getWords();
    }

    /**
     * Returns true if the text contains any blacklisted word or phrase (case-insensitive).
     */
    public boolean containsBlacklistedWord(String text) {
        if (text == null || text.isBlank()) return false;

        String normalized = text.toLowerCase();

        return blacklistedWords.stream()
                .map(String::toLowerCase)
                .anyMatch(normalized::contains);
    }
}
