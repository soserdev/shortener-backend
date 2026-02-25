package dev.smo.shortener.backend.ratelimit;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

import static java.time.Duration.ofMinutes;

@Slf4j
@Profile("!test")
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.ratelimit.capacity:20}")
    private long capacity;

    @Value("${app.ratelimit.refill:20}")
    private int refill;

    @Value("${app.ratelimit.minutes:1}")
    private int minutes;

    @Value("${app.ratelimit.initialTokens:10}")
    private int initialTokens;


    private final ProxyManager<String> proxyManager;

    public RateLimitFilter(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    private BucketConfiguration createNewBucket(long capacity, long refill, long minutes, long initial) {
        return BucketConfiguration.builder()
                // Tokens are added gradually over time
                .addLimit(limit -> limit.capacity(capacity).refillIntervally(refill, ofMinutes(minutes)).initialTokens(initial))
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {


        // ⭐ Skip preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Assure a certain ip address does not exceed its own limit
        var clientIP = getRemoteIp(request);
        var clientIPKey = "rate-limit:" + clientIP;
        var bucketPerIp = proxyManager.builder().build(clientIPKey, createNewBucket(capacity, refill, minutes, initialTokens));
        var ipLimitOk = bucketPerIp.tryConsume(1); // consume if max-limit is not reached

        long remainingTokens = bucketPerIp.getAvailableTokens();
        log.info("RATE-LIMIT-IP: " + clientIP + " Available Tokens: " + remainingTokens);

        if (ipLimitOk) {
            filterChain.doFilter(request, response);
        } else {
            // Limit exceeded
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            // These must be present even on error responses
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
            response.setHeader("Access-Control-Allow-Credentials", "true");

            response.getWriter().write("{"
                    + "\"timestamp\":\"" + LocalDateTime.now() + "\", "
                    + "\"status\": " + HttpStatus.TOO_MANY_REQUESTS.value() + ", "
                    + "\"error\":\"" + HttpStatus.TOO_MANY_REQUESTS.toString() + "\", "
                    + "\"message\":\"Your Rate Limit has exceeded\""
                    + "}");
        }
    }

    String getRemoteIp(HttpServletRequest request) {
        var clientIp = request.getHeader("CF-Connecting-IP");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("X-Forwarded-For");
            if (clientIp != null && clientIp.contains(",")) {
                clientIp = clientIp.split(",")[0].trim();
            }
        }
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}
