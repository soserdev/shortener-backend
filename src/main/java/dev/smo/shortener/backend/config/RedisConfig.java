package dev.smo.shortener.backend.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@Configuration
@Profile("!test")
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Bean
    public RedisClient redisClient(LettuceConnectionFactory connectionFactory) {
        if (!(connectionFactory instanceof LettuceConnectionFactory lettuceFactory)) {
            throw new IllegalStateException("ConnectionFactory is not Lettuce");
        }
        RedisURI redisURI = RedisURI.builder()
                .withHost(lettuceFactory.getHostName())
                .withPort(lettuceFactory.getPort())
//               .withPassword(lettuceFactory.getPassword())
                .build();

        return RedisClient.create(redisURI);
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(host);
        redisConfig.setPort(port);
        log.info(String.format("REDIS : %s:%s", host, port));
        // Optional: redisConfig.setPassword(RedisPassword.of("yourpassword"));
        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public StringRedisTemplate redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }
}
