package com.scalefirstai.evolution.carddemo.cards;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for CardController.
 * Validates behavioral equivalence with COCRDLIC.cbl (card listing)
 * and COCRDUPC.cbl (card detail/update) programs.
 *
 * COBOL source references:
 * - COCRDLIC.cbl: PROCESS-PAGE-FORWARD, PROCESS-PAGE-BACKWARD paragraphs
 * - COCRDUPC.cbl: PROCESS-ENTER-KEY, EDIT-CARD-CHANGES paragraphs
 * - Data structures: CARD-RECORD in CVACT03Y.cpy
 */
@QuarkusTest
class CardControllerTest {

    // --- Card Listing (COCRDLIC equivalence) ---

    @Test
    void testListCardsByAccountId() {
        given()
            .queryParam("accountId", "00000000001")
        .when()
            .get("/api/v1/cards")
        .then()
            .statusCode(200)
            .body("cards", hasSize(greaterThan(0)))
            .body("cards[0].accountId", equalTo("00000000001"))
            .body("cards[0].cardNumber", notNullValue());
    }

    @Test
    void testListCardsWithPagination() {
        given()
            .queryParam("accountId", "00000000001")
            .queryParam("page", 0)
            .queryParam("size", 5)
        .when()
            .get("/api/v1/cards")
        .then()
            .statusCode(200)
            .body("cards", hasSize(lessThanOrEqualTo(5)))
            .body("totalCount", greaterThanOrEqualTo(0))
            .body("page", equalTo(0));
    }

    @Test
    void testListCardsPaginationSecondPage() {
        given()
            .queryParam("accountId", "00000000001")
            .queryParam("page", 1)
            .queryParam("size", 5)
        .when()
            .get("/api/v1/cards")
        .then()
            .statusCode(200)
            .body("page", equalTo(1));
    }

    @Test
    void testListCardsNoAccountId() {
        given()
        .when()
            .get("/api/v1/cards")
        .then()
            .statusCode(400);
    }

    @Test
    void testListCardsInvalidAccountId() {
        given()
            .queryParam("accountId", "INVALID")
        .when()
            .get("/api/v1/cards")
        .then()
            .statusCode(400);
    }

    @Test
    void testListCardsNonExistentAccount() {
        given()
            .queryParam("accountId", "99999999999")
        .when()
            .get("/api/v1/cards")
        .then()
            .statusCode(200)
            .body("cards", hasSize(0))
            .body("totalCount", equalTo(0));
    }

    // --- Card Detail (COCRDUPC equivalence) ---

    @Test
    void testGetCardByNumber() {
        given()
        .when()
            .get("/api/v1/cards/{cardNumber}", "4111111111111111")
        .then()
            .statusCode(200)
            .body("cardNumber", equalTo("4111111111111111"))
            .body("accountId", notNullValue())
            .body("cardStatus", notNullValue())
            .body("expiryDate", notNullValue());
    }

    @Test
    void testGetCardNotFound() {
        given()
        .when()
            .get("/api/v1/cards/{cardNumber}", "0000000000000000")
        .then()
            .statusCode(404);
    }

    @Test
    void testGetCardInvalidNumberFormat() {
        given()
        .when()
            .get("/api/v1/cards/{cardNumber}", "ABCD")
        .then()
            .statusCode(400);
    }

    // --- Card Update (COCRDUPC EDIT-CARD-CHANGES equivalence) ---

    @Test
    void testUpdateCardStatus() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"cardStatus\": \"N\"}")
        .when()
            .put("/api/v1/cards/{cardNumber}", "4111111111111111")
        .then()
            .statusCode(200)
            .body("cardStatus", equalTo("N"));
    }

    @Test
    void testUpdateCardExpiryDate() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"expiryDate\": \"2028-12-31\"}")
        .when()
            .put("/api/v1/cards/{cardNumber}", "4111111111111111")
        .then()
            .statusCode(200)
            .body("expiryDate", equalTo("2028-12-31"));
    }

    @Test
    void testUpdateCardInvalidStatus() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"cardStatus\": \"X\"}")
        .when()
            .put("/api/v1/cards/{cardNumber}", "4111111111111111")
        .then()
            .statusCode(400);
    }

    @Test
    void testUpdateCardPastExpiryDate() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"expiryDate\": \"2020-01-01\"}")
        .when()
            .put("/api/v1/cards/{cardNumber}", "4111111111111111")
        .then()
            .statusCode(400);
    }

    @Test
    void testUpdateCardNotFound() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"cardStatus\": \"Y\"}")
        .when()
            .put("/api/v1/cards/{cardNumber}", "0000000000000000")
        .then()
            .statusCode(404);
    }

    @Test
    void testUpdateCardEmptyBody() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .put("/api/v1/cards/{cardNumber}", "4111111111111111")
        .then()
            .statusCode(400);
    }

    @Test
    void testCardResponseContainsAllFields() {
        given()
        .when()
            .get("/api/v1/cards/{cardNumber}", "4111111111111111")
        .then()
            .statusCode(200)
            .body("cardNumber", notNullValue())
            .body("accountId", notNullValue())
            .body("cardStatus", notNullValue())
            .body("expiryDate", notNullValue())
            .body("cardholderName", notNullValue());
    }

    @Test
    void testListCardsResponseFormat() {
        given()
            .queryParam("accountId", "00000000001")
        .when()
            .get("/api/v1/cards")
        .then()
            .statusCode(200)
            .body("cards", notNullValue())
            .body("totalCount", notNullValue())
            .body("page", notNullValue())
            .body("size", notNullValue());
    }

    @Test
    void testCardStatusValues() {
        // Valid statuses: Y (Active), N (Inactive), R (Reported Lost)
        given()
            .contentType(ContentType.JSON)
            .body("{\"cardStatus\": \"Y\"}")
        .when()
            .put("/api/v1/cards/{cardNumber}", "4111111111111111")
        .then()
            .statusCode(200)
            .body("cardStatus", isOneOf("Y", "N", "R"));
    }

    @Test
    void testListCardsDefaultPageSize() {
        given()
            .queryParam("accountId", "00000000001")
        .when()
            .get("/api/v1/cards")
        .then()
            .statusCode(200)
            .body("size", equalTo(10));
    }

    @Test
    void testListCardsNegativePage() {
        given()
            .queryParam("accountId", "00000000001")
            .queryParam("page", -1)
        .when()
            .get("/api/v1/cards")
        .then()
            .statusCode(400);
    }

    @Test
    void testUpdateCardMalformedJson() {
        given()
            .contentType(ContentType.JSON)
            .body("{invalid}")
        .when()
            .put("/api/v1/cards/{cardNumber}", "4111111111111111")
        .then()
            .statusCode(400);
    }

    @Test
    void testUpdateCardReportedLost() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"cardStatus\": \"R\"}")
        .when()
            .put("/api/v1/cards/{cardNumber}", "4111111111111111")
        .then()
            .statusCode(200)
            .body("cardStatus", equalTo("R"));
    }

    @Test
    void testGetCardContentTypeJson() {
        given()
        .when()
            .get("/api/v1/cards/{cardNumber}", "4111111111111111")
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"));
    }
}
