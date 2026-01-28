package dev.smo.shortener.backend.util;


import java.net.URI;

public class UrlUtils {

    public static boolean isValidURL(String urlToCheck) {
        if (urlToCheck == null || urlToCheck.isBlank()) {
            return false;
        }

        try {
            URI uri = new URI(urlToCheck);

            // Must be absolute
            if (!uri.isAbsolute()) {
                return false;
            }

            // Only allow http / https
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                return false;
            }

            // Must have a host (prevents "http:/test")
            if (uri.getHost() == null) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractHost(String input) {
        try {
            URI uri = new URI(input);
            return uri.getHost(); // may be null if invalid
        } catch (Exception e) {
            return null;
        }
    }
}
