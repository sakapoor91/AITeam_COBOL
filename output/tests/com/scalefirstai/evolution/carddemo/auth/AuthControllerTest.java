package com.scalefirstai.evolution.carddemo.auth;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for AuthController.
 * Validates behavioral equivalence with COSGN00C.cbl PROCESS-ENTER-KEY paragraph.
 */
@QuarkusTest
class AuthControllerTest {

    @Test
    void testLoginSuccess() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"USER0001\", \"password\": \"PASS0001\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .body("userId", equalTo("USER0001"))
            .body("userType", notNullValue())
            .body("token", notNullValue());
    }

    @Test
    void testLoginInvalidPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"USER0001\", \"password\": \"WRONG\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    void testLoginInvalidUser() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"NOUSER\", \"password\": \"PASS0001\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    void testLoginEmptyFields() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    void testLoginNullUserId() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": null, \"password\": \"PASS0001\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    void testLoginNullPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"USER0001\", \"password\": null}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    void testLoginBlankUserId() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"   \", \"password\": \"PASS0001\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    void testLoginBlankPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"USER0001\", \"password\": \"   \"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    void testLoginResponseContainsUserType() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"ADMIN001\", \"password\": \"ADMINPASS\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .body("userType", equalTo("ADMIN"));
    }

    @Test
    void testLoginMaxLengthUserId() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\", \"password\": \"PASS0001\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    void testLoginCaseInsensitiveUserId() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"user0001\", \"password\": \"PASS0001\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .body("userId", equalTo("USER0001"));
    }

    @Test
    void testLoginResponseTokenFormat() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"USER0001\", \"password\": \"PASS0001\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .body("token", matchesPattern("^[A-Za-z0-9\\-_\\.]+$"));
    }

    @Test
    void testLoginWrongContentType() {
        given()
            .contentType(ContentType.TEXT)
            .body("userId=USER0001&password=PASS0001")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(415);
    }

    @Test
    void testLoginLockedAccount() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"LOCKED01\", \"password\": \"PASS0001\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(403);
    }

    @Test
    void testLoginMalformedJson() {
        given()
            .contentType(ContentType.JSON)
            .body("{bad json}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    void testLoginSqlInjectionAttempt() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"' OR 1=1 --\", \"password\": \"test\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    void testLoginSpecialCharactersInPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"USER0001\", \"password\": \"P@$$w0rd!\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    void testLoginResponseHeaders() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"userId\": \"USER0001\", \"password\": \"PASS0001\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"));
    }
}
