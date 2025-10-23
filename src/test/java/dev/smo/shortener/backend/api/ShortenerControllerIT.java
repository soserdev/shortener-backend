package dev.smo.shortener.backend.api;

import dev.smo.shortener.backend.TestcontainersConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesRegex;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ShortenerControllerIT {

    @LocalServerPort
    private int port;

    @PostConstruct
    public void init() {
        System.out.println("Running ShortenerControllerIT...");
        RestAssured.baseURI = "http://localhost/";
        RestAssured.port = port;
    }

    @Test
    void getAuthorWithExistingId() {
        var url = "http://www.example.com/test";
        var idRegex = "^[0-9a-fA-F]{24}$";

        String id = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{ \"url\": \"" + url + "\" }")
                .when()
                    .post("/shorturl")
                .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .contentType(ContentType.JSON)
                    .body("id", matchesRegex(idRegex))
                    .body("url", equalTo(url))
                    .body("shortUrl", equalTo("1fa"))
                //.extract().toString(); to get the whole json as string
                .extract().path("id");
        log.info(id);
    }

}