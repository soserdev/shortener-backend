package dev.smo.shortener.backend;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.DockerComposeContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@Slf4j
@ActiveProfiles("test")
class ShortenerBackendApplicationIT {

	@Autowired
	DockerComposeContainer<?> composeContainer;

	@Test
	void contextLoads() {
		Integer keygenPort = composeContainer.getServicePort("keygenerator", 8081);
		Integer redisPort = composeContainer.getServicePort("redis", 6379);
        log.info("Keygen port: {}", keygenPort);
        log.info("Redis port: {}", redisPort);
	}

}
