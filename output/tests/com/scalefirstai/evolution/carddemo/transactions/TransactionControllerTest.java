package com.scalefirstai.evolution.carddemo.transactions;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for TransactionController.
 * Validates behavioral equivalence with COTRN00C.cbl (online transaction
 * listing and processing).
 *
 * COBOL source references:
 * - COTRN00C.cbl: PROCESS-ENTER-KEY, PROCESS-PAGE-FORWARD,
 *                  VALIDATE-TRANSACTION-DATA paragraphs
 * - Data structures: TRAN-RECORD in CVTRA05Y.cpy
 * - File: TRANSACT (KSDS VSAM, key = TRAN-ID)
 *
 * Key business rules:
 * - Transaction amounts must be positive (PIC S9(9)V99)
 * - Transaction types: 01 (Purchase), 02 (Return), 03 (Payment), 04 (Cash Advance)
 * - All monetary values use BigDecimal with scale 2
 * - Card must be active (status Y) for new transactions
 */
@QuarkusTest
class TransactionControllerTest {

    // --- Transaction Listing ---

    @Test
    void testListTransactions() {
        given()
            .queryParam("accountId", "00000000001")
        .when()
            .get("/api/v1/transactions")
        .then()
            .statusCode(200)
            .body("transactions", hasSize(greaterThan(0)))
            .body("transactions[0].transactionId", notNullValue())
            .body("transactions[0].amount", notNullValue());
    }

    @Test
    void testListTransactionsWithPagination() {
        given()
            .queryParam("accountId", "00000000001")
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/v1/transactions")
        .then()
            .statusCode(200)
            .body("transactions", hasSize(lessThanOrEqualTo(10)))
            .body("totalCount", greaterThanOrEqualTo(0))
            .body("page", equalTo(0));
    }

    @Test
    void testListTransactionsNoAccountId() {
        given()
        .when()
            .get("/api/v1/transactions")
        .then()
            .statusCode(400);
    }

    @Test
    void testListTransactionsNonExistentAccount() {
        given()
            .queryParam("accountId", "99999999999")
        .when()
            .get("/api/v1/transactions")
        .then()
            .statusCode(200)
            .body("transactions", hasSize(0))
            .body("totalCount", equalTo(0));
    }

    @Test
    void testListTransactionsByCardNumber() {
        given()
            .queryParam("cardNumber", "4111111111111111")
        .when()
            .get("/api/v1/transactions")
        .then()
            .statusCode(200)
            .body("transactions", hasSize(greaterThan(0)));
    }

    @Test
    void testListTransactionsDateRange() {
        given()
            .queryParam("accountId", "00000000001")
            .queryParam("fromDate", "2026-01-01")
            .queryParam("toDate", "2026-03-22")
        .when()
            .get("/api/v1/transactions")
        .then()
            .statusCode(200)
            .body("transactions", notNullValue());
    }

    @Test
    void testListTransactionsInvalidDateRange() {
        given()
            .queryParam("accountId", "00000000001")
            .queryParam("fromDate", "2026-12-31")
            .queryParam("toDate", "2026-01-01")
        .when()
            .get("/api/v1/transactions")
        .then()
            .statusCode(400);
    }

    // --- Transaction Creation (VALIDATE-TRANSACTION-DATA equivalence) ---

    @Test
    void testCreatePurchaseTransaction() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"cardNumber\": \"4111111111111111\"," +
                "\"transactionType\": \"01\"," +
                "\"amount\": 150.75," +
                "\"merchantId\": \"MERCHANT001\"," +
                "\"description\": \"Purchase at store\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(201)
            .body("transactionId", notNullValue())
            .body("amount", equalTo(150.75f))
            .body("transactionType", equalTo("01"))
            .body("status", equalTo("POSTED"));
    }

    @Test
    void testCreateReturnTransaction() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"cardNumber\": \"4111111111111111\"," +
                "\"transactionType\": \"02\"," +
                "\"amount\": 50.00," +
                "\"merchantId\": \"MERCHANT001\"," +
                "\"description\": \"Return at store\"," +
                "\"originalTransactionId\": \"TXN00000001\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(201)
            .body("transactionType", equalTo("02"));
    }

    @Test
    void testCreatePaymentTransaction() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"cardNumber\": \"4111111111111111\"," +
                "\"transactionType\": \"03\"," +
                "\"amount\": 500.00," +
                "\"description\": \"Monthly payment\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(201)
            .body("transactionType", equalTo("03"));
    }

    @Test
    void testCreateTransactionInvalidAmount() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"cardNumber\": \"4111111111111111\"," +
                "\"transactionType\": \"01\"," +
                "\"amount\": -100.00," +
                "\"merchantId\": \"MERCHANT001\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateTransactionZeroAmount() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"cardNumber\": \"4111111111111111\"," +
                "\"transactionType\": \"01\"," +
                "\"amount\": 0.00," +
                "\"merchantId\": \"MERCHANT001\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateTransactionInvalidType() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"cardNumber\": \"4111111111111111\"," +
                "\"transactionType\": \"99\"," +
                "\"amount\": 100.00," +
                "\"merchantId\": \"MERCHANT001\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateTransactionInactiveCard() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"cardNumber\": \"4222222222222222\"," +
                "\"transactionType\": \"01\"," +
                "\"amount\": 100.00," +
                "\"merchantId\": \"MERCHANT001\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(409);
    }

    @Test
    void testCreateTransactionExceedsCreditLimit() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"cardNumber\": \"4111111111111111\"," +
                "\"transactionType\": \"01\"," +
                "\"amount\": 99999999.99," +
                "\"merchantId\": \"MERCHANT001\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(422);
    }

    @Test
    void testCreateTransactionMissingCardNumber() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"transactionType\": \"01\"," +
                "\"amount\": 100.00," +
                "\"merchantId\": \"MERCHANT001\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateTransactionNonExistentCard() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"cardNumber\": \"0000000000000000\"," +
                "\"transactionType\": \"01\"," +
                "\"amount\": 100.00," +
                "\"merchantId\": \"MERCHANT001\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(404);
    }

    @Test
    void testCreateTransactionEmptyBody() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateTransactionMalformedJson() {
        given()
            .contentType(ContentType.JSON)
            .body("{invalid}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(400);
    }

    @Test
    void testTransactionAmountPrecision() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"cardNumber\": \"4111111111111111\"," +
                "\"transactionType\": \"01\"," +
                "\"amount\": 99.99," +
                "\"merchantId\": \"MERCHANT001\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(201)
            .body("amount", equalTo(99.99f));
    }

    @Test
    void testTransactionAmountExcessivePrecision() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"cardNumber\": \"4111111111111111\"," +
                "\"transactionType\": \"01\"," +
                "\"amount\": 99.999," +
                "\"merchantId\": \"MERCHANT001\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateCashAdvanceTransaction() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"cardNumber\": \"4111111111111111\"," +
                "\"transactionType\": \"04\"," +
                "\"amount\": 200.00," +
                "\"description\": \"ATM withdrawal\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(201)
            .body("transactionType", equalTo("04"));
    }

    @Test
    void testListTransactionsDefaultPageSize() {
        given()
            .queryParam("accountId", "00000000001")
        .when()
            .get("/api/v1/transactions")
        .then()
            .statusCode(200)
            .body("size", equalTo(20));
    }

    @Test
    void testListTransactionsResponseFormat() {
        given()
            .queryParam("accountId", "00000000001")
        .when()
            .get("/api/v1/transactions")
        .then()
            .statusCode(200)
            .body("transactions", notNullValue())
            .body("totalCount", notNullValue())
            .body("page", notNullValue())
            .body("size", notNullValue());
    }

    @Test
    void testTransactionResponseContainsTimestamp() {
        given()
            .contentType(ContentType.JSON)
            .body("{" +
                "\"cardNumber\": \"4111111111111111\"," +
                "\"transactionType\": \"01\"," +
                "\"amount\": 25.00," +
                "\"merchantId\": \"MERCHANT001\"" +
            "}")
        .when()
            .post("/api/v1/transactions")
        .then()
            .statusCode(201)
            .body("timestamp", notNullValue());
    }

    @Test
    void testTransactionResponseJsonContentType() {
        given()
            .queryParam("accountId", "00000000001")
        .when()
            .get("/api/v1/transactions")
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"));
    }
}
