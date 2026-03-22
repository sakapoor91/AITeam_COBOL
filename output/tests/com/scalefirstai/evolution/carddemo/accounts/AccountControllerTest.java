package com.scalefirstai.evolution.carddemo.accounts;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for AccountController.
 * Validates behavioral equivalence with COACTUPC.cbl program.
 *
 * COBOL source references:
 * - COACTUPC.cbl: PROCESS-ENTER-KEY, EDIT-ACCT-CHANGES paragraphs
 * - Data structures: ACCOUNT-RECORD in CVACT01Y.cpy
 * - File: ACCTDAT (KSDS VSAM, key = ACCT-ID)
 *
 * Key business rules:
 * - Credit limit must be non-negative (PIC S9(15)V99)
 * - Account status: Y (Active), N (Inactive), C (Closed)
 * - All monetary values use BigDecimal with scale 2
 */
@QuarkusTest
class AccountControllerTest {

    // --- Account Retrieval ---

    @Test
    void testGetAccountById() {
        given()
        .when()
            .get("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(200)
            .body("accountId", equalTo("00000000001"))
            .body("creditLimit", notNullValue())
            .body("currentBalance", notNullValue())
            .body("accountStatus", notNullValue());
    }

    @Test
    void testGetAccountNotFound() {
        given()
        .when()
            .get("/api/v1/accounts/{id}", "99999999999")
        .then()
            .statusCode(404);
    }

    @Test
    void testGetAccountInvalidIdFormat() {
        given()
        .when()
            .get("/api/v1/accounts/{id}", "INVALID")
        .then()
            .statusCode(400);
    }

    @Test
    void testGetAccountContainsAllFields() {
        given()
        .when()
            .get("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(200)
            .body("accountId", notNullValue())
            .body("accountStatus", notNullValue())
            .body("creditLimit", notNullValue())
            .body("currentBalance", notNullValue())
            .body("cashCreditLimit", notNullValue())
            .body("openDate", notNullValue())
            .body("expirationDate", notNullValue())
            .body("reissueDate", notNullValue())
            .body("customerId", notNullValue())
            .body("groupId", notNullValue());
    }

    // --- Account Update (EDIT-ACCT-CHANGES equivalence) ---

    @Test
    void testUpdateAccountCreditLimit() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"creditLimit\": 15000.00}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(200)
            .body("creditLimit", equalTo(15000.00f));
    }

    @Test
    void testUpdateAccountNegativeCreditLimit() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"creditLimit\": -5000.00}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(400);
    }

    @Test
    void testUpdateAccountZeroCreditLimit() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"creditLimit\": 0.00}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(200)
            .body("creditLimit", equalTo(0.00f));
    }

    @Test
    void testUpdateAccountStatus() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"accountStatus\": \"N\"}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(200)
            .body("accountStatus", equalTo("N"));
    }

    @Test
    void testUpdateAccountInvalidStatus() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"accountStatus\": \"Z\"}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(400);
    }

    @Test
    void testUpdateAccountNotFound() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"creditLimit\": 10000.00}")
        .when()
            .put("/api/v1/accounts/{id}", "99999999999")
        .then()
            .statusCode(404);
    }

    @Test
    void testUpdateAccountEmptyBody() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(400);
    }

    @Test
    void testUpdateAccountMalformedJson() {
        given()
            .contentType(ContentType.JSON)
            .body("{bad json}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(400);
    }

    // --- Monetary Precision ---

    @Test
    void testCreditLimitPrecision() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"creditLimit\": 10000.99}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(200)
            .body("creditLimit", equalTo(10000.99f));
    }

    @Test
    void testCreditLimitExcessiveDecimalPlaces() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"creditLimit\": 10000.999}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(400);
    }

    @Test
    void testUpdateAccountCashCreditLimit() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"cashCreditLimit\": 5000.00}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(200)
            .body("cashCreditLimit", equalTo(5000.00f));
    }

    @Test
    void testUpdateAccountNegativeCashCreditLimit() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"cashCreditLimit\": -1000.00}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(400);
    }

    @Test
    void testAccountMaxCreditLimit() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"creditLimit\": 9999999999999.99}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(200);
    }

    @Test
    void testAccountOverflowCreditLimit() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"creditLimit\": 99999999999999.99}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(400);
    }

    @Test
    void testGetAccountJsonContentType() {
        given()
        .when()
            .get("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"));
    }

    @Test
    void testUpdateAccountCloseStatus() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"accountStatus\": \"C\"}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(200)
            .body("accountStatus", equalTo("C"));
    }

    @Test
    void testUpdateClosedAccountFails() {
        // Closed accounts should reject further updates per COBOL EDIT-ACCT-CHANGES
        given()
            .contentType(ContentType.JSON)
            .body("{\"creditLimit\": 50000.00}")
        .when()
            .put("/api/v1/accounts/{id}", "00000000099")
        .then()
            .statusCode(409);
    }

    @Test
    void testGetAccountBalanceIsDecimal() {
        given()
        .when()
            .get("/api/v1/accounts/{id}", "00000000001")
        .then()
            .statusCode(200)
            .body("currentBalance.toString()", matchesPattern("-?\\d+\\.\\d{2}"));
    }
}
