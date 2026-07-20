package com.studyflow.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class HealthResourceTest {
    @Test
    void testHealthEndpoint() {
        given()
          .when().get("/api/v1/health")
          .then()
             .statusCode(200)
             .body(is("OK"));
    }

}