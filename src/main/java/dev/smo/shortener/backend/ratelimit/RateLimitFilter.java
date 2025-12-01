package dev.smo.shortener.backend.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

import static java.time.Duration.ofMinutes;

@Profile("!test")
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<String> proxyManager;

    public RateLimitFilter(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    private BucketConfiguration createNewBucket() {
        return BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(10).refillIntervally(10, ofMinutes(1)).initialTokens(10))
                .build();
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
        String clientIP = "rate-limit:" + request.getRemoteAddr();
        Bucket bucket = proxyManager.builder().build(clientIP, this::createNewBucket);
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
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
}
