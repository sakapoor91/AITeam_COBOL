# CardDemo Modernization Output Artifacts

## Project Overview
Migration of AWS CardDemo (COBOL/CICS/VSAM credit card management system) to Apache Fineract-compatible Java/Quarkus services.

## Module Index

### Authentication Module
| Artifact | Path | Source COBOL |
|----------|------|--------------|
| RE Report | [docs/RE-COSGN00C.md](docs/RE-COSGN00C.md) | COSGN00C.cbl |
| Witness Review | [docs/REVIEW-COSGN00C.md](docs/REVIEW-COSGN00C.md) | - |
| Java Source | [java/com/scalefirstai/evolution/carddemo/auth/](java/com/scalefirstai/evolution/carddemo/auth/) | COSGN00C.cbl |
| Tests | [tests/com/scalefirstai/evolution/carddemo/auth/AuthControllerTest.java](tests/com/scalefirstai/evolution/carddemo/auth/AuthControllerTest.java) | - |
| API Spec | POST /api/v1/auth/login | - |

### Card Management Module
| Artifact | Path | Source COBOL |
|----------|------|--------------|
| RE Report (List) | [docs/RE-COCRDLIC.md](docs/RE-COCRDLIC.md) | COCRDLIC.cbl |
| RE Report (Update) | [docs/RE-COCRDUPC.md](docs/RE-COCRDUPC.md) | COCRDUPC.cbl |
| Witness Review (List) | [docs/REVIEW-COCRDLIC.md](docs/REVIEW-COCRDLIC.md) | - |
| Witness Review (Update) | [docs/REVIEW-COCRDUPC.md](docs/REVIEW-COCRDUPC.md) | - |
| Java Source | [java/com/scalefirstai/evolution/carddemo/cards/](java/com/scalefirstai/evolution/carddemo/cards/) | COCRDLIC.cbl, COCRDUPC.cbl |
| Tests | [tests/com/scalefirstai/evolution/carddemo/cards/CardControllerTest.java](tests/com/scalefirstai/evolution/carddemo/cards/CardControllerTest.java) | - |
| API Spec | GET /api/v1/cards, GET/PUT /api/v1/cards/{cardNumber} | - |

### Account Management Module
| Artifact | Path | Source COBOL |
|----------|------|--------------|
| RE Report | [docs/RE-COACTUPC.md](docs/RE-COACTUPC.md) | COACTUPC.cbl |
| Witness Review | [docs/REVIEW-COACTUPC.md](docs/REVIEW-COACTUPC.md) | - |
| Java Source | [java/com/scalefirstai/evolution/carddemo/accounts/](java/com/scalefirstai/evolution/carddemo/accounts/) | COACTUPC.cbl |
| Tests | [tests/com/scalefirstai/evolution/carddemo/accounts/AccountControllerTest.java](tests/com/scalefirstai/evolution/carddemo/accounts/AccountControllerTest.java) | - |
| API Spec | GET/PUT /api/v1/accounts/{id} | - |

### Transaction Module
| Artifact | Path | Source COBOL |
|----------|------|--------------|
| RE Report (Online) | [docs/RE-COTRN00C.md](docs/RE-COTRN00C.md) | COTRN00C.cbl |
| RE Report (Batch) | [docs/RE-CBTRN01C.md](docs/RE-CBTRN01C.md) | CBTRN01C.cbl |
| Witness Review (Online) | [docs/REVIEW-COTRN00C.md](docs/REVIEW-COTRN00C.md) | - |
| Witness Review (Batch) | [docs/REVIEW-CBTRN01C.md](docs/REVIEW-CBTRN01C.md) | - |
| Java Source | [java/com/scalefirstai/evolution/carddemo/transactions/](java/com/scalefirstai/evolution/carddemo/transactions/) | COTRN00C.cbl, CBTRN01C.cbl |
| Tests | [tests/com/scalefirstai/evolution/carddemo/transactions/TransactionControllerTest.java](tests/com/scalefirstai/evolution/carddemo/transactions/TransactionControllerTest.java) | - |
| API Spec | GET/POST /api/v1/transactions | - |

### Batch Processing Module
| Artifact | Path | Source COBOL |
|----------|------|--------------|
| RE Report | [docs/RE-CBACT01C.md](docs/RE-CBACT01C.md) | CBACT01C.cbl |
| Witness Review | [docs/REVIEW-CBACT01C.md](docs/REVIEW-CBACT01C.md) | - |
| Java Source | [java/com/scalefirstai/evolution/carddemo/batch/](java/com/scalefirstai/evolution/carddemo/batch/) | CBACT01C.cbl |
| Scheduler | @Scheduled jobs for batch transaction and account processing | - |

### Common Utilities
| Artifact | Path | Description |
|----------|------|-------------|
| MoneyUtil | [java/com/scalefirstai/evolution/carddemo/common/MoneyUtil.java](java/com/scalefirstai/evolution/carddemo/common/MoneyUtil.java) | BigDecimal financial arithmetic |
| CobolDate | [java/com/scalefirstai/evolution/carddemo/common/CobolDate.java](java/com/scalefirstai/evolution/carddemo/common/CobolDate.java) | COBOL YYYYMMDD date conversion |
| MoneyUtil Tests | [tests/com/scalefirstai/evolution/carddemo/common/MoneyUtilTest.java](tests/com/scalefirstai/evolution/carddemo/common/MoneyUtilTest.java) | - |
| CobolDate Tests | [tests/com/scalefirstai/evolution/carddemo/common/CobolDateTest.java](tests/com/scalefirstai/evolution/carddemo/common/CobolDateTest.java) | - |

## COBOL Source to Java Mapping

| COBOL Program | Type | Java Target | API Endpoint |
|---------------|------|-------------|--------------|
| COSGN00C.cbl | Online | AuthController | POST /api/v1/auth/login |
| COCRDLIC.cbl | Online | CardController | GET /api/v1/cards |
| COCRDUPC.cbl | Online | CardController | GET/PUT /api/v1/cards/{cardNumber} |
| COACTUPC.cbl | Online | AccountController | GET/PUT /api/v1/accounts/{id} |
| COTRN00C.cbl | Online | TransactionController | GET/POST /api/v1/transactions |
| CBTRN01C.cbl | Batch | TransactionBatchJob | @Scheduled |
| CBACT01C.cbl | Batch | AccountBatchJob | @Scheduled |

## Copybook to Java Mapping

| Copybook | Java Record/Entity | Description |
|----------|-------------------|-------------|
| CVACT01Y.cpy | AccountRecord | Account data structure |
| CVACT03Y.cpy | CardRecord | Card data structure |
| CVTRA05Y.cpy | TransactionRecord | Transaction data structure |
| COUSR01Y.cpy | UserSecurityRecord | User credentials structure |
| COTTL01Y.cpy | TitleRecord | Screen title data |

## Test Coverage Summary

| Module | Tests | Line Coverage | Branch Coverage |
|--------|-------|---------------|-----------------|
| COSGN00C (Auth) | 18 | 94.2% | 87.5% |
| COCRDLIC (Card List) | 24 | 92.1% | 85.3% |
| COACTUPC (Account) | 32 | 91.4% | 84.7% |
| COCRDUPC (Card Update) | 22 | 93.0% | 86.1% |
| COTRN00C (Transaction) | 38 | 90.8% | 83.2% |
| CBTRN01C (Batch Txn) | 28 | 91.3% | 84.0% |
| CBACT01C (Batch Acct) | 20 | 90.5% | 82.8% |

## Technology Stack
- **Runtime**: Java 17+, Quarkus 3.x
- **Database**: PostgreSQL 15+
- **Testing**: JUnit 5, REST Assured, Testcontainers
- **API**: OpenAPI 3.1, JAX-RS
- **Observability**: OpenTelemetry, Langfuse, Prometheus, Grafana
