package dev.smo.shortener.backend.util;

import java.util.regex.Pattern;

public class URLValidator {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[\\w.-]+(?:\\.[\\w.-]+)+[/#?]?.*$",
            Pattern.CASE_INSENSITIVE
    );

    public static boolean isValidURL(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }
}
