package dev.smo.shortener.backend.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?)://[\\w.-]+(?:\\.[\\w.-]+)+[/#?]?.*$",
            Pattern.CASE_INSENSITIVE
    );

    public static boolean isValidURL(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }

    // Regex breakdown:
    // ^https?://        → required http or https
    // ([^/:?#]+)        → capture host (up to port, path, or query)
    private static final Pattern HOST_PATTERN =
            Pattern.compile("^(?:https?)://([^/:?#]+)");
//            Pattern.compile("^(?:https?|ftp)://([^/:?#]+)");

    public static String extractHost(String url) {
        if (url == null) return null;

        Matcher matcher = HOST_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
