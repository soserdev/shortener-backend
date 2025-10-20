package dev.smo.shortener.backend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    DockerComposeContainer<?> composeContainer() {
        DockerComposeContainer service = new DockerComposeContainer<>(new File("compose.yaml"))
                .withExposedService("redis", 6379)
                .withExposedService("keygenerator", 8081, Wait.forHttp("/actuator/health"))
                .withExposedService("mongo", 27017)
                .withExposedService("urlservice", 8082, Wait.forHttp("/actuator/health"));
        return service;
    }

}
