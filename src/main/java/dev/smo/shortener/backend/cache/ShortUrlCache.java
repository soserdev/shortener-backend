package dev.smo.shortener.backend.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class ShortUrlCache {

    private static final String CACHE_PREFIX = "shortener:cache:shorturl:";

    @Value("${shortener.backend.cache.shorturl.timeout:180}")
    private long timeout;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ShortUrlCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // cache the url without a protocol...
    public void setCachedUrl(String id, String shortUrl, String longUrl) {
        var key = CACHE_PREFIX + shortUrl;
        var value = "{ \"id\": \"" + id + "\""
                + ", \"shorturl\": \"" + shortUrl  + "\""
                + ", \"url\": \"" + longUrl + "\""
                + "}";
        // log.info("Set Cached Url: {}", value);
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    // get the cached url like getCachedUrl("snib.me/1fa")
    public CachedUrl getCachedUrl(String shortUrl) {
        var key = CACHE_PREFIX + shortUrl;
        final String json = redisTemplate.opsForValue().getAndExpire(key, timeout, TimeUnit.SECONDS);
        if (null == json) {
            return null;
        }
        // log.info("Get Cached Url: {}", json);
        try {
            return objectMapper.readValue(json, CachedUrl.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    public void removeCachedUrl(String shortUrl) {
        var key = CACHE_PREFIX + shortUrl;
        Boolean deleted = redisTemplate.delete(key);

        if (Boolean.TRUE.equals(deleted)) {
            log.info("Removed cached URL for key: {}", key);
        } else {
            log.warn("No cached URL found to remove for key: {}", key);
        }
    }
}
