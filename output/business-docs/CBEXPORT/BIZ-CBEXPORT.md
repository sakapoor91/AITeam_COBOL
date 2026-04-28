# CBEXPORT — Customer Data Export for Branch Migration

```
Application : AWS CardDemo
Source File : CBEXPORT.cbl
Type        : Batch COBOL
Source Banner: Program     : CBEXPORT.CBL
```

This document describes what the program does in plain English. It treats the program as a sequence of data actions — reading from five normalized CardDemo files, mapping fields into a single multi-record export format, and writing statistics — and names every file, field, copybook, and external program along the way so a developer can still find each piece in the source. The reader does not need to know COBOL.

---

## 1. Purpose

CBEXPORT is a **branch migration export** program. Its business purpose is to consolidate all CardDemo operational data — customers, accounts, cross-references, transactions, and cards — into a single flat indexed export file for transfer to another branch or system. Each source entity becomes one or more fixed-500-byte records in the export file, distinguished by a one-character record type code.

The program reads five input files sequentially, in this order: customers, accounts, cross-references, transactions, and cards. For each record read from each source file, it assembles an `EXPORT-RECORD` (defined in copybook `CVEXPORT`) and writes it to the export output file. Statistics are accumulated per entity type and displayed at the end.

**Files read (input):**

- `CUSTOMER-INPUT` (DDname `CUSTFILE`) — the Customer Master File, read sequentially.
- `ACCOUNT-INPUT` (DDname `ACCTFILE`) — the Account Master File, read sequentially.
- `XREF-INPUT` (DDname `XREFFILE`) — the Card Cross-Reference File, read sequentially.
- `TRANSACTION-INPUT` (DDname `TRANSACT`) — the Transaction File, read sequentially.
- `CARD-INPUT` (DDname `CARDFILE`) — the Card Master File, read sequentially.

**File written (output):**

- `EXPORT-OUTPUT` (DDname `EXPFILE`) — a fixed-format indexed file, 500 bytes per record, keyed on `EXPORT-SEQUENCE-NUM`. One record per source entity, with record type codes distinguishing entity types: `'C'` = customer, `'A'` = account, `'X'` = cross-reference, `'T'` = transaction, `'D'` = card.

**External programs called:** `CEE3ABD` for abend only.

**Important hardcoded values:**
- `EXPORT-BRANCH-ID` is always `'0001'` — a hardcoded branch identifier that is not derived from any input data.
- `EXPORT-REGION-CODE` is always `'NORTH'` — a hardcoded region that is not derived from any input data. Both values must not be treated as real business rules during migration.

---

## 2. Program Flow

The program runs in three phases: **startup** (initialize timestamp and open all six files), **per-entity processing** (five sequential passes, one per entity type), and **finalize** (close all files and print statistics).

### 2.1 Startup

The main control paragraph `0000-MAIN-PROCESSING` (line 149) calls the sub-phases in order. Each performs a specific pass over one entity type before the next begins.

**Step 1 — Initialize** *(paragraph `1000-INITIALIZE`, line 161).* Displays the banner `'CBEXPORT: Starting Customer Data Export'`, generates the export timestamp, and opens all six files.

**Step 1a — Generate timestamp** *(paragraph `1050-GENERATE-TIMESTAMP`, line 172).* Accepts the current date using `ACCEPT WS-CURRENT-DATE FROM DATE YYYYMMDD` and the current time using `ACCEPT WS-CURRENT-TIME FROM TIME`. Formats `WS-EXPORT-DATE` as `YYYY-MM-DD` and `WS-EXPORT-TIME` as `HH:MM:SS` using STRING concatenation. Builds a 26-character `WS-FORMATTED-TIMESTAMP` as `'YYYY-MM-DD HH:MM:SS.00'` — note the `.00` suffix is a hardcoded literal, not real sub-second precision (see Migration Note 1).

**Step 1b — Open all six files** *(paragraph `1100-OPEN-FILES`, line 198).* Opens all five input files for input and the export output file for output. Each open checks the corresponding 88-level condition (`WS-CUSTOMER-OK`, `WS-ACCOUNT-OK`, etc.). If any open fails, the program displays `'ERROR: Cannot open <filename>, Status: '` followed by the status code, then calls `9999-ABEND-PROGRAM`. Note: unlike most of the suite, this program does **not** call a formatted status decoder before abending — it concatenates the raw two-byte status directly onto the display string.

### 2.2 Per-Entity Processing Passes

Five sequential passes follow. Each pass uses the same read-loop-then-write pattern.

#### Pass 1 — Customer Export *(paragraph `2000-EXPORT-CUSTOMERS`, line 243)*

Displays `'CBEXPORT: Processing customer records'`, reads the first record, then loops until `WS-CUSTOMER-EOF` is true. For each record:

- `2100-READ-CUSTOMER-RECORD` *(line 258):* Reads from `CUSTOMER-INPUT` (which uses copybook `CVCUS01Y` as its FD layout) into the implicit FD buffer. If the status is neither `'00'` nor `'10'`, displays `'ERROR: Reading CUSTOMER-INPUT, Status: '` and abends.
- `2200-CREATE-CUSTOMER-EXP-REC` *(line 269):* Initialises `EXPORT-RECORD`. Sets `EXPORT-REC-TYPE` to `'C'`, `EXPORT-TIMESTAMP` to `WS-FORMATTED-TIMESTAMP`, increments `WS-SEQUENCE-COUNTER` and places it in `EXPORT-SEQUENCE-NUM`, sets `EXPORT-BRANCH-ID` to `'0001'` and `EXPORT-REGION-CODE` to `'NORTH'`. Then maps all customer fields from `CVCUS01Y` fields to the `EXPORT-CUSTOMER-DATA` REDEFINES overlay in `CVEXPORT`. After mapping, writes `EXPORT-OUTPUT-RECORD` (a flat 500-byte PIC X field) using the `FROM EXPORT-RECORD` phrase. Increments both `WS-CUSTOMER-RECORDS-EXPORTED` and `WS-TOTAL-RECORDS-EXPORTED`.

Customer field mappings (source → export):

| Source field (`CVCUS01Y`) | Export field (`CVEXPORT` — `EXPORT-CUSTOMER-DATA`) | Notes |
|---|---|---|
| `CUST-ID` | `EXP-CUST-ID` PIC 9(09) COMP | **COMP-3 mismatch:** source is display `9(09)`, export is `COMP`. See Migration Note 2. |
| `CUST-FIRST-NAME` | `EXP-CUST-FIRST-NAME` X(25) | Direct copy |
| `CUST-MIDDLE-NAME` | `EXP-CUST-MIDDLE-NAME` X(25) | Direct copy |
| `CUST-LAST-NAME` | `EXP-CUST-LAST-NAME` X(25) | Direct copy |
| `CUST-ADDR-LINE-1` | `EXP-CUST-ADDR-LINE(1)` X(50) | First element of OCCURS 3 |
| `CUST-ADDR-LINE-2` | `EXP-CUST-ADDR-LINE(2)` X(50) | Second element |
| `CUST-ADDR-LINE-3` | `EXP-CUST-ADDR-LINE(3)` X(50) | Third element |
| `CUST-ADDR-STATE-CD` | `EXP-CUST-ADDR-STATE-CD` X(02) | Direct copy |
| `CUST-ADDR-COUNTRY-CD` | `EXP-CUST-ADDR-COUNTRY-CD` X(03) | Direct copy |
| `CUST-ADDR-ZIP` | `EXP-CUST-ADDR-ZIP` X(10) | Direct copy |
| `CUST-PHONE-NUM-1` | `EXP-CUST-PHONE-NUM(1)` X(15) | First element of OCCURS 2 |
| `CUST-PHONE-NUM-2` | `EXP-CUST-PHONE-NUM(2)` X(15) | Second element |
| `CUST-SSN` | `EXP-CUST-SSN` 9(09) | PII — social security number exported in plaintext |
| `CUST-GOVT-ISSUED-ID` | `EXP-CUST-GOVT-ISSUED-ID` X(20) | PII |
| `CUST-DOB-YYYY-MM-DD` | `EXP-CUST-DOB-YYYY-MM-DD` X(10) | PII |
| `CUST-EFT-ACCOUNT-ID` | `EXP-CUST-EFT-ACCOUNT-ID` X(10) | Sensitive |
| `CUST-PRI-CARD-HOLDER-IND` | `EXP-CUST-PRI-CARD-HOLDER-IND` X(01) | Direct copy |
| `CUST-FICO-CREDIT-SCORE` | `EXP-CUST-FICO-CREDIT-SCORE` 9(03) COMP-3 | **Type change:** source is display `9(03)`, export is COMP-3 (COMP-3 — use BigDecimal in Java). See Migration Note 3. |
| `CUST-ID` `FILLER` X(168) | `FILLER` X(134) in export | Source filler is 168 bytes; export customer data filler is only 134 bytes. Size mismatch; extra 34 bytes from source FILLER overflow into export filler. See Migration Note 4. |

At the end of Pass 1, displays `'CBEXPORT: Customers exported: '` followed by `WS-CUSTOMER-RECORDS-EXPORTED`.

#### Pass 2 — Account Export *(paragraph `3000-EXPORT-ACCOUNTS`, line 312)*

Same pattern. `3100-READ-ACCOUNT-RECORD` *(line 327)* reads from `ACCOUNT-INPUT`. `3200-CREATE-ACCOUNT-EXP-REC` *(line 338)* sets record type `'A'` and maps account fields.

Account field mappings (source `CVACT01Y` → export `CVEXPORT EXPORT-ACCOUNT-DATA`):

| Source field | Export field | PIC / Notes |
|---|---|---|
| `ACCT-ID` | `EXP-ACCT-ID` | PIC 9(11) display → 9(11) display — no conversion |
| `ACCT-ACTIVE-STATUS` | `EXP-ACCT-ACTIVE-STATUS` | X(01) → X(01) |
| `ACCT-CURR-BAL` | `EXP-ACCT-CURR-BAL` | S9(10)V99 display → **S9(10)V99 COMP-3** (COMP-3 — use BigDecimal in Java). Type change on export. |
| `ACCT-CREDIT-LIMIT` | `EXP-ACCT-CREDIT-LIMIT` | S9(10)V99 display → S9(10)V99 display — no conversion |
| `ACCT-CASH-CREDIT-LIMIT` | `EXP-ACCT-CASH-CREDIT-LIMIT` | S9(10)V99 display → **S9(10)V99 COMP-3** (COMP-3 — use BigDecimal in Java). Type change. |
| `ACCT-OPEN-DATE` | `EXP-ACCT-OPEN-DATE` | X(10) → X(10) |
| `ACCT-EXPIRAION-DATE` | `EXP-ACCT-EXPIRAION-DATE` | X(10) → X(10) — typo preserved in both source and export field names |
| `ACCT-REISSUE-DATE` | `EXP-ACCT-REISSUE-DATE` | X(10) → X(10) |
| `ACCT-CURR-CYC-CREDIT` | `EXP-ACCT-CURR-CYC-CREDIT` | S9(10)V99 display → S9(10)V99 display |
| `ACCT-CURR-CYC-DEBIT` | `EXP-ACCT-CURR-CYC-DEBIT` | S9(10)V99 display → **S9(10)V99 COMP** (binary — use BigDecimal). Type change. |
| `ACCT-ADDR-ZIP` | `EXP-ACCT-ADDR-ZIP` | X(10) → X(10) — **this field is unused in CBACT01C and CBACT04C but IS mapped here** |
| `ACCT-GROUP-ID` | `EXP-ACCT-GROUP-ID` | X(10) → X(10) |

At the end of Pass 2, displays `'CBEXPORT: Accounts exported: '` followed by `WS-ACCOUNT-RECORDS-EXPORTED`.

#### Pass 3 — Cross-Reference Export *(paragraph `4000-EXPORT-XREFS`, line 376)*

`4100-READ-XREF-RECORD` *(line 391)* reads from `XREF-INPUT`. `4200-CREATE-XREF-EXPORT-RECORD` *(line 402)* sets record type `'X'` and maps the three xref fields.

Cross-reference field mappings (`CVACT03Y` → `CVEXPORT EXPORT-CARD-XREF-DATA`):

| Source field | Export field | Notes |
|---|---|---|
| `XREF-CARD-NUM` | `EXP-XREF-CARD-NUM` X(16) | Direct copy |
| `XREF-CUST-ID` | `EXP-XREF-CUST-ID` 9(09) | Display → display |
| `XREF-ACCT-ID` | `EXP-XREF-ACCT-ID` 9(11) | Display → **9(11) COMP** (binary). Type change on export. See Migration Note 5. |
| `FILLER` X(14) | `FILLER` X(427) | Source filler not mapped; export pads remaining 427 bytes |

#### Pass 4 — Transaction Export *(paragraph `5000-EXPORT-TRANSACTIONS`, line 431)*

`5100-READ-TRANSACTION-RECORD` *(line 446)* reads from `TRANSACTION-INPUT`. `5200-CREATE-TRAN-EXP-REC` *(line 457)* sets record type `'T'` and maps transaction fields.

Transaction field mappings (`CVTRA05Y` → `CVEXPORT EXPORT-TRANSACTION-DATA`):

| Source field | Export field | Notes |
|---|---|---|
| `TRAN-ID` | `EXP-TRAN-ID` X(16) | Direct copy |
| `TRAN-TYPE-CD` | `EXP-TRAN-TYPE-CD` X(02) | Direct copy |
| `TRAN-CAT-CD` | `EXP-TRAN-CAT-CD` 9(04) | Display → display |
| `TRAN-SOURCE` | `EXP-TRAN-SOURCE` X(10) | Direct copy |
| `TRAN-DESC` | `EXP-TRAN-DESC` X(100) | Direct copy |
| `TRAN-AMT` | `EXP-TRAN-AMT` S9(09)V99 | Display → **COMP-3** (COMP-3 — use BigDecimal in Java). Type change. |
| `TRAN-MERCHANT-ID` | `EXP-TRAN-MERCHANT-ID` 9(09) | Display → **COMP** (binary). Type change. |
| `TRAN-MERCHANT-NAME` | `EXP-TRAN-MERCHANT-NAME` X(50) | Direct copy |
| `TRAN-MERCHANT-CITY` | `EXP-TRAN-MERCHANT-CITY` X(50) | Direct copy |
| `TRAN-MERCHANT-ZIP` | `EXP-TRAN-MERCHANT-ZIP` X(10) | Direct copy |
| `TRAN-CARD-NUM` | `EXP-TRAN-CARD-NUM` X(16) | Direct copy |
| `TRAN-ORIG-TS` | `EXP-TRAN-ORIG-TS` X(26) | Direct copy |
| `TRAN-PROC-TS` | `EXP-TRAN-PROC-TS` X(26) | Direct copy |
| `FILLER` X(20) | `FILLER` X(140) | Not mapped |

#### Pass 5 — Card Export *(paragraph `5500-EXPORT-CARDS`, line 496)*

`5600-READ-CARD-RECORD` *(line 511)* reads from `CARD-INPUT`. `5700-CREATE-CARD-EXPORT-RECORD` *(line 522)* sets record type `'D'` (not `'C'`, which is reserved for customer — this is the card record type, using `'D'` presumably for "debit card" or to avoid collision).

Card field mappings (`CVACT02Y` → `CVEXPORT EXPORT-CARD-DATA`):

| Source field | Export field | Notes |
|---|---|---|
| `CARD-NUM` | `EXP-CARD-NUM` X(16) | Direct copy — full PAN (Primary Account Number) exported in plaintext |
| `CARD-ACCT-ID` | `EXP-CARD-ACCT-ID` 9(11) | Display → **COMP** (binary). Type change. |
| `CARD-CVV-CD` | `EXP-CARD-CVV-CD` 9(03) | Display → **COMP** (binary). Type change. CVV exported — PCI-DSS concern. |
| `CARD-EMBOSSED-NAME` | `EXP-CARD-EMBOSSED-NAME` X(50) | Direct copy |
| `CARD-EXPIRAION-DATE` | `EXP-CARD-EXPIRAION-DATE` X(10) | Typo preserved in both source and export — misspelled EXPIRAION |
| `CARD-ACTIVE-STATUS` | `EXP-CARD-ACTIVE-STATUS` X(01) | Direct copy |
| `FILLER` X(59) | `FILLER` X(373) | Not mapped |

### 2.3 Finalize (Shutdown)

**Step 2 — Finalize** *(paragraph `6000-FINALIZE`, line 554).* Closes all six files in order: `CUSTOMER-INPUT`, `ACCOUNT-INPUT`, `XREF-INPUT`, `TRANSACTION-INPUT`, `CARD-INPUT`, `EXPORT-OUTPUT`. Close operations have **no error checking** — if a close fails, no error is displayed and processing continues to the summary (see Migration Note 6).

Then displays the export summary:
- `'CBEXPORT: Export completed'`
- `'CBEXPORT: Customers Exported: '` + `WS-CUSTOMER-RECORDS-EXPORTED`
- `'CBEXPORT: Accounts Exported: '` + `WS-ACCOUNT-RECORDS-EXPORTED`
- `'CBEXPORT: XRefs Exported: '` + `WS-XREF-RECORDS-EXPORTED`
- `'CBEXPORT: Transactions Exported: '` + `WS-TRAN-RECORDS-EXPORTED`
- `'CBEXPORT: Cards Exported: '` + `WS-CARD-RECORDS-EXPORTED`
- `'CBEXPORT: Total Records Exported: '` + `WS-TOTAL-RECORDS-EXPORTED`

---

## 3. Error Handling

### 3.1 File Open / Read Errors

Each open and read paragraph checks its file status condition (`WS-xxx-OK` or `WS-xxx-EOF`) immediately after the I/O. On an unexpected status, the error display concatenates the raw status code directly to the message string rather than calling a formatted decoder:
- Open failures: `'ERROR: Cannot open <FILENAME>, Status: '` + two-byte status code.
- Read failures: `'ERROR: Reading <FILENAME>, Status: '` + two-byte status code.

### 3.2 Abend Routine — `9999-ABEND-PROGRAM` (line 576)

Displays `'CBEXPORT: ABENDING PROGRAM'` and calls `CEE3ABD` with **no parameters** (`CALL 'CEE3ABD'` with no USING clause). This is structurally different from the other programs in the suite, which always pass `ABCODE` and `TIMING`. Calling `CEE3ABD` without parameters relies on whatever defaults the Language Environment runtime supplies; the resulting abend code is not guaranteed to be `U999` and may vary by LE version (see Migration Note 7).

---

## 4. Migration Notes

1. **`WS-FORMATTED-TIMESTAMP` always ends with `.00`** *(paragraph `1050-GENERATE-TIMESTAMP`, line 191).* The 26-character timestamp is built by appending the literal `'.00'` after the `HH:MM:SS` portion. The result is always `'YYYY-MM-DD HH:MM:SS.00'` regardless of actual system time. The microsecond and sub-second portions are never captured. Any importer expecting a precise sub-second timestamp will receive a constant `.00` suffix.

2. **`CUST-ID` (display `9(09)`) is moved into `EXP-CUST-ID` (COMP binary)** *(line 282, CVEXPORT line 25).* `EXP-CUST-ID` is `PIC 9(09) COMP` — binary storage. A COBOL MOVE from a display numeric to a COMP field performs an implicit conversion, but the export record is written as a flat 500-byte X field, meaning the binary bytes are written as-is. An importer reading the record must be aware that this 4-byte field contains a big-endian binary integer, not a 9-digit display numeric. (COMP — use a 4-byte integer or long in Java).

3. **`CUST-FICO-CREDIT-SCORE` (display `9(03)`) is written as `EXP-CUST-FICO-CREDIT-SCORE` (COMP-3)** *(CVEXPORT line 41).* The COBOL MOVE converts the 3-digit display numeric to 2-byte packed decimal. The importer must decode it as COMP-3 (COMP-3 — use BigDecimal in Java). A Java importer treating this as a display character field will read garbage.

4. **Customer data FILLER size mismatch.** `CVCUS01Y` declares `FILLER X(168)` (the last 168 bytes of the 500-byte customer record). `CVEXPORT EXPORT-CUSTOMER-DATA` declares `FILLER X(134)`. The total payload within `EXPORT-RECORD-DATA` for a customer record is: 4 (COMP ID) + 25 + 25 + 25 + 150 + 2 + 3 + 10 + 30 + 9 + 20 + 10 + 10 + 1 + 2 (COMP-3 FICO) + 134 FILLER = 460 bytes, matching the 460-byte `EXPORT-RECORD-DATA` field. The source customer record's 168-byte filler does not all appear in the export — only the non-filler fields are mapped. This is intentional field remapping, not a data loss issue, as long as the importer reads the export layout rather than the source layout.

5. **`XREF-ACCT-ID` (display `9(11)`) is written as `EXP-XREF-ACCT-ID` (COMP)** *(CVEXPORT line 87).* The export field is `PIC 9(11) COMP` — a binary value. On most z/OS systems `9(11)` COMP occupies 8 bytes (a double-word). An importer must decode this as an 8-byte big-endian binary integer (Java `long`).

6. **Close operations in `6000-FINALIZE` have no error checking** *(lines 556–561).* Every close is executed without checking the resulting file status. A write-buffer flush error during close of `EXPORT-OUTPUT` would be silent — the export file could be incomplete without any indication in the job log.

7. **`CEE3ABD` is called without parameters** *(paragraph `9999-ABEND-PROGRAM`, line 579).* The Language Environment service `CEE3ABD` expects two parameters (`ABCODE` and `TIMING`). Calling it with no USING clause passes whatever happens to be on the parameter stack at the time, which is undefined behaviour. The resulting abend code may not be `U999` and may vary across LE versions or JVM states.

8. **Full card PAN and CVV are written to the export file** *(paragraph `5700-CREATE-CARD-EXPORT-RECORD`).* `CARD-NUM` (the full 16-digit card number) and `CARD-CVV-CD` are mapped directly into the export record without any masking or encryption. PCI-DSS prohibits storing CVV values on any medium after authorization; writing them to a migration export file is a compliance violation.

9. **`EXPORT-SEQUENCE-NUM` is `PIC 9(9) COMP`** *(CVEXPORT line 16).* The sequence counter is a binary field within the 500-byte `EXPORT-RECORD`. It also serves as the VSAM KSDS key for `EXPORT-OUTPUT`. If more than 999,999,999 records are exported, the sequence counter wraps, causing duplicate key errors. For a typical CardDemo dataset this is not a practical risk, but the limit should be documented.

10. **`EXPORT-BRANCH-ID = '0001'` and `EXPORT-REGION-CODE = 'NORTH'` are hardcoded** *(lines 279, 280, 347, 348, 411, 412, 465, 466, 530, 531).* These values appear in every export record regardless of the actual branch or region being migrated. In any multi-branch deployment, the same export file would be generated with identical branch and region metadata regardless of source. These must be parameterized in any production implementation.

---

## Appendix A — Files

| Logical Name | DDname | Organization | Recording | Key Field | Direction | Contents |
|---|---|---|---|---|---|---|
| `CUSTOMER-INPUT` | `CUSTFILE` | VSAM KSDS — indexed, accessed sequentially | Fixed, 500 bytes | `CUST-ID` 9(09) | Input — read-only | Customer master records (layout: `CVCUS01Y`) |
| `ACCOUNT-INPUT` | `ACCTFILE` | VSAM KSDS — indexed, accessed sequentially | Fixed, 300 bytes | `ACCT-ID` 9(11) | Input — read-only | Account master records (layout: `CVACT01Y`) |
| `XREF-INPUT` | `XREFFILE` | VSAM KSDS — indexed, accessed sequentially | Fixed, 50 bytes | `XREF-CARD-NUM` X(16) | Input — read-only | Card cross-reference records (layout: `CVACT03Y`) |
| `TRANSACTION-INPUT` | `TRANSACT` | VSAM KSDS — indexed, accessed sequentially | Fixed, 350 bytes | `TRAN-ID` X(16) | Input — read-only | Transaction records (layout: `CVTRA05Y`) |
| `CARD-INPUT` | `CARDFILE` | VSAM KSDS — indexed, accessed sequentially | Fixed, 150 bytes | `CARD-NUM` X(16) | Input — read-only | Card master records (layout: `CVACT02Y`) |
| `EXPORT-OUTPUT` | `EXPFILE` | VSAM KSDS — indexed, sequential write | Fixed, 500 bytes | `EXPORT-SEQUENCE-NUM` 9(9) COMP | Output — sequential write | Multi-record export file. One 500-byte row per source entity. Record types: `'C'`=customer, `'A'`=account, `'X'`=xref, `'T'`=transaction, `'D'`=card. |

---

## Appendix B — Copybooks and External Programs

### Copybook `CVCUS01Y` (FILE SECTION — FD `CUSTOMER-INPUT`, line 75)

Defines `CUSTOMER-RECORD`. See CBCUS01C appendix for full field table. All 18 data fields plus `FILLER X(168)` are present. All data fields are mapped to the export record; the filler is not.

### Copybook `CVACT01Y` (FILE SECTION — FD `ACCOUNT-INPUT`, line 78)

Defines `ACCOUNT-RECORD`. See CBACT01C appendix for full field table. All 12 data fields plus `FILLER X(178)` are present. All 12 data fields are mapped to the export record. This program **does** map `ACCT-ADDR-ZIP` to `EXP-ACCT-ADDR-ZIP` (line 361) — this is the only program in the suite that exports this field (CBACT01C silently drops it).

### Copybook `CVACT03Y` (FILE SECTION — FD `XREF-INPUT`, line 81)

Defines `CARD-XREF-RECORD`. Three data fields plus `FILLER X(14)`. All three data fields are mapped.

### Copybook `CVTRA05Y` (FILE SECTION — FD `TRANSACTION-INPUT`, line 84)

Defines `TRAN-RECORD`. 13 data fields plus `FILLER X(20)`. All 13 data fields are mapped.

### Copybook `CVACT02Y` (FILE SECTION — FD `CARD-INPUT`, line 87)

Defines `CARD-RECORD`. Six data fields plus `FILLER X(59)`. All six data fields are mapped.

### Copybook `CVEXPORT` (WORKING-STORAGE SECTION, line 96)

Defines `EXPORT-RECORD` — the 500-byte export record written to `EXPORT-OUTPUT`. Source file: `CVEXPORT.cpy`.

**Common header fields (present in every record type):**

| Field | PIC | Bytes | Notes |
|---|---|---|---|
| `EXPORT-REC-TYPE` | `X(1)` | 1 | Record type discriminator: `'C'`=customer, `'A'`=account, `'X'`=xref, `'T'`=transaction, `'D'`=card |
| `EXPORT-TIMESTAMP` | `X(26)` | 26 | Export run timestamp; REDEFINES provides `EXPORT-DATE` X(10), `EXPORT-DATE-TIME-SEP` X(1), `EXPORT-TIME` X(15) |
| `EXPORT-SEQUENCE-NUM` | `9(9) COMP` | 4 | Sequential record number; also serves as VSAM KSDS key. COMP — binary integer in Java. |
| `EXPORT-BRANCH-ID` | `X(4)` | 4 | Hardcoded `'0001'` |
| `EXPORT-REGION-CODE` | `X(5)` | 5 | Hardcoded `'NORTH'` |
| `EXPORT-RECORD-DATA` | `X(460)` | 460 | REDEFINES overlay for all entity-type-specific data (see below) |

**`EXPORT-CUSTOMER-DATA` REDEFINES `EXPORT-RECORD-DATA`** (460 bytes):

| Field | PIC | Notes |
|---|---|---|
| `EXP-CUST-ID` | `9(09) COMP` | Binary 4-byte integer (COMP — use int/long in Java) |
| `EXP-CUST-FIRST-NAME` | `X(25)` | |
| `EXP-CUST-MIDDLE-NAME` | `X(25)` | |
| `EXP-CUST-LAST-NAME` | `X(25)` | |
| `EXP-CUST-ADDR-LINES OCCURS 3` / `EXP-CUST-ADDR-LINE` | `X(50)` each | Three address lines |
| `EXP-CUST-ADDR-STATE-CD` | `X(02)` | |
| `EXP-CUST-ADDR-COUNTRY-CD` | `X(03)` | |
| `EXP-CUST-ADDR-ZIP` | `X(10)` | |
| `EXP-CUST-PHONE-NUMS OCCURS 2` / `EXP-CUST-PHONE-NUM` | `X(15)` each | Two phone numbers |
| `EXP-CUST-SSN` | `9(09)` | PII — plaintext SSN |
| `EXP-CUST-GOVT-ISSUED-ID` | `X(20)` | PII |
| `EXP-CUST-DOB-YYYY-MM-DD` | `X(10)` | PII |
| `EXP-CUST-EFT-ACCOUNT-ID` | `X(10)` | |
| `EXP-CUST-PRI-CARD-HOLDER-IND` | `X(01)` | |
| `EXP-CUST-FICO-CREDIT-SCORE` | `9(03) COMP-3` | (COMP-3 — use BigDecimal in Java); 2 bytes packed |
| `FILLER` | `X(134)` | Unused padding |

**`EXPORT-ACCOUNT-DATA` REDEFINES `EXPORT-RECORD-DATA`** (460 bytes):

| Field | PIC | Notes |
|---|---|---|
| `EXP-ACCT-ID` | `9(11)` | Display |
| `EXP-ACCT-ACTIVE-STATUS` | `X(01)` | |
| `EXP-ACCT-CURR-BAL` | `S9(10)V99 COMP-3` | (COMP-3 — use BigDecimal in Java); 7 bytes packed |
| `EXP-ACCT-CREDIT-LIMIT` | `S9(10)V99` | Display |
| `EXP-ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99 COMP-3` | (COMP-3 — use BigDecimal in Java); 7 bytes packed |
| `EXP-ACCT-OPEN-DATE` | `X(10)` | |
| `EXP-ACCT-EXPIRAION-DATE` | `X(10)` | Typo — EXPIRAION; spelling preserved |
| `EXP-ACCT-REISSUE-DATE` | `X(10)` | |
| `EXP-ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | Display |
| `EXP-ACCT-CURR-CYC-DEBIT` | `S9(10)V99 COMP` | Binary — COMP (use BigDecimal in Java) |
| `EXP-ACCT-ADDR-ZIP` | `X(10)` | |
| `EXP-ACCT-GROUP-ID` | `X(10)` | |
| `FILLER` | `X(352)` | |

**`EXPORT-TRANSACTION-DATA` REDEFINES `EXPORT-RECORD-DATA`** (460 bytes):

| Field | PIC | Notes |
|---|---|---|
| `EXP-TRAN-ID` | `X(16)` | |
| `EXP-TRAN-TYPE-CD` | `X(02)` | |
| `EXP-TRAN-CAT-CD` | `9(04)` | Display |
| `EXP-TRAN-SOURCE` | `X(10)` | |
| `EXP-TRAN-DESC` | `X(100)` | |
| `EXP-TRAN-AMT` | `S9(09)V99 COMP-3` | (COMP-3 — use BigDecimal in Java); 6 bytes packed |
| `EXP-TRAN-MERCHANT-ID` | `9(09) COMP` | Binary integer |
| `EXP-TRAN-MERCHANT-NAME` | `X(50)` | |
| `EXP-TRAN-MERCHANT-CITY` | `X(50)` | |
| `EXP-TRAN-MERCHANT-ZIP` | `X(10)` | |
| `EXP-TRAN-CARD-NUM` | `X(16)` | Full PAN |
| `EXP-TRAN-ORIG-TS` | `X(26)` | |
| `EXP-TRAN-PROC-TS` | `X(26)` | |
| `FILLER` | `X(140)` | |

**`EXPORT-CARD-XREF-DATA` REDEFINES `EXPORT-RECORD-DATA`** (460 bytes):

| Field | PIC | Notes |
|---|---|---|
| `EXP-XREF-CARD-NUM` | `X(16)` | |
| `EXP-XREF-CUST-ID` | `9(09)` | Display |
| `EXP-XREF-ACCT-ID` | `9(11) COMP` | Binary 8-byte integer |
| `FILLER` | `X(427)` | |

**`EXPORT-CARD-DATA` REDEFINES `EXPORT-RECORD-DATA`** (460 bytes):

| Field | PIC | Notes |
|---|---|---|
| `EXP-CARD-NUM` | `X(16)` | Full PAN — PCI concern |
| `EXP-CARD-ACCT-ID` | `9(11) COMP` | Binary integer |
| `EXP-CARD-CVV-CD` | `9(03) COMP` | Binary integer — CVV exported; PCI-DSS violation |
| `EXP-CARD-EMBOSSED-NAME` | `X(50)` | |
| `EXP-CARD-EXPIRAION-DATE` | `X(10)` | Typo — EXPIRAION |
| `EXP-CARD-ACTIVE-STATUS` | `X(01)` | |
| `FILLER` | `X(373)` | |

### External Service `CEE3ABD`

| Item | Detail |
|---|---|
| Type | IBM Language Environment runtime service for forced abend |
| Called from | Paragraph `9999-ABEND-PROGRAM`, line 579 |
| Parameters | **None passed** — CALL has no USING clause, unlike all other programs in the suite. Abend code is uncontrolled. |

---

## Appendix C — Hardcoded Literals

| Paragraph | Line | Value | Usage | Classification |
|---|---|---|---|---|
| `1000-INITIALIZE` | 163 | `'CBEXPORT: Starting Customer Data Export'` | Start banner | Display message |
| `1050-GENERATE-TIMESTAMP` | 191 | `'.00'` | Sub-second portion of timestamp | System constant — not real precision |
| `1050-GENERATE-TIMESTAMP` | 179, 181, 185, 187 | `'-'`, `':'` | Date and time separators in timestamp | System constant |
| `2200-CREATE-CUSTOMER-EXP-REC` | 274 | `'C'` | Customer record type code | Business rule |
| `2200-CREATE-CUSTOMER-EXP-REC` | 279 | `'0001'` | Branch ID — hardcoded | Test data / not a real branch |
| `2200-CREATE-CUSTOMER-EXP-REC` | 280 | `'NORTH'` | Region code — hardcoded | Test data / not a real region |
| `3200-CREATE-ACCOUNT-EXP-REC` | 343 | `'A'` | Account record type code | Business rule |
| `3200-CREATE-ACCOUNT-EXP-REC` | 347, 348 | `'0001'`, `'NORTH'` | Branch ID and region — hardcoded | Test data |
| `4200-CREATE-XREF-EXPORT-RECORD` | 407 | `'X'` | Cross-reference record type code | Business rule |
| `4200-CREATE-XREF-EXPORT-RECORD` | 411, 412 | `'0001'`, `'NORTH'` | Branch ID and region — hardcoded | Test data |
| `5200-CREATE-TRAN-EXP-REC` | 462 | `'T'` | Transaction record type code | Business rule |
| `5200-CREATE-TRAN-EXP-REC` | 465, 466 | `'0001'`, `'NORTH'` | Branch ID and region — hardcoded | Test data |
| `5700-CREATE-CARD-EXPORT-RECORD` | 527 | `'D'` | Card record type code | Business rule |
| `5700-CREATE-CARD-EXPORT-RECORD` | 530, 531 | `'0001'`, `'NORTH'` | Branch ID and region — hardcoded | Test data |

---

## Appendix D — Internal Working Fields

| Field | PIC | Bytes | Purpose |
|---|---|---|---|
| `WS-CUSTOMER-STATUS` (and 5 others) | `X(02)` each | 2 each | File status codes for each of the six files; each has 88-level `WS-xxx-OK = '00'` and `WS-xxx-EOF = '10'` |
| `WS-EXPORT-DATE` | `X(10)` | 10 | Formatted export date `YYYY-MM-DD` |
| `WS-EXPORT-TIME` | `X(08)` | 8 | Formatted export time `HH:MM:SS` |
| `WS-FORMATTED-TIMESTAMP` | `X(26)` | 26 | 26-character timestamp placed in every export record header |
| `WS-SEQUENCE-COUNTER` | `9(09)` | 9 | Monotonically increasing counter; becomes `EXPORT-SEQUENCE-NUM` (the VSAM key) |
| `WS-CURRENT-DATE` with `WS-CURR-YEAR`, `WS-CURR-MONTH`, `WS-CURR-DAY` | `9(04)`, `9(02)`, `9(02)` | 8 total | Date components from `ACCEPT ... FROM DATE YYYYMMDD` |
| `WS-CURRENT-TIME` with `WS-CURR-HOUR`, `WS-CURR-MINUTE`, `WS-CURR-SECOND`, `WS-CURR-HUNDREDTH` | `9(02)` each | 8 total | Time components from `ACCEPT ... FROM TIME` |
| `WS-CUSTOMER-RECORDS-EXPORTED` (and 4 per-type counters) | `9(09)` each | 9 each | Count of records exported per entity type |
| `WS-TOTAL-RECORDS-EXPORTED` | `9(09)` | 9 | Total across all entity types |

---

## Appendix E — Execution at a Glance

```mermaid
flowchart TD
    START([CBEXPORT]) --> MAIN

    subgraph MAIN ["0000-MAIN-PROCESSING · line 149"]
        MAIN["Sequential phase calls:\n1000-INITIALIZE\n2000-EXPORT-CUSTOMERS\n3000-EXPORT-ACCOUNTS\n4000-EXPORT-XREFS\n5000-EXPORT-TRANSACTIONS\n5500-EXPORT-CARDS\n6000-FINALIZE\nGOBACK"]
    end

    MAIN --> INIT

    subgraph PH1 ["Phase 1 — Startup"]
        INIT["1000-INITIALIZE · line 161\nDisplay start banner\nGenerate timestamp (1050)\nOpen all six files (1100)"]
        INIT --> TST["1050-GENERATE-TIMESTAMP\nACCEPT date + time\nBuild WS-FORMATTED-TIMESTAMP\n'.00' suffix hardcoded"]
        TST --> OPEN["1100-OPEN-FILES · line 198\nOpen CUSTOMER-INPUT · CUSTFILE\nOpen ACCOUNT-INPUT · ACCTFILE\nOpen XREF-INPUT · XREFFILE\nOpen TRANSACTION-INPUT · TRANSACT\nOpen CARD-INPUT · CARDFILE\nOpen EXPORT-OUTPUT · EXPFILE"]
        OPEN -->|Any error| OPERR["Display error + raw status\nAbend — no status decoder"]
    end

    OPERR --> ABEND
    OPEN --> P2

    subgraph P2 ["Phase 2a — Customer Pass  ·  2000-EXPORT-CUSTOMERS"]
        RC["2100-READ-CUSTOMER-RECORD"]
        RC -->|WS-CUSTOMER-EOF| NEXT_PASS_A
        RC -->|WS-CUSTOMER-OK| WC["2200-CREATE-CUSTOMER-EXP-REC\nSet type 'C'\nBranch='0001' Region='NORTH'\nMap all CVCUS01Y fields\nWrite to EXPORT-OUTPUT"]
        WC --> RC
    end

    NEXT_PASS_A --> P3

    subgraph P3 ["Phase 2b — Account Pass  ·  3000-EXPORT-ACCOUNTS"]
        RA["3100-READ-ACCOUNT-RECORD"]
        RA -->|WS-ACCOUNT-EOF| NEXT_PASS_B
        RA -->|WS-ACCOUNT-OK| WA["3200-CREATE-ACCOUNT-EXP-REC\nSet type 'A'\nMap all CVACT01Y fields incl. ACCT-ADDR-ZIP\nCOMP-3 conversions for curr-bal, cash-limit"]
        WA --> RA
    end

    NEXT_PASS_B --> P4

    subgraph P4 ["Phase 2c — Xref Pass  ·  4000-EXPORT-XREFS"]
        RX["4100-READ-XREF-RECORD"]
        RX -->|WS-XREF-EOF| NEXT_PASS_C
        RX -->|WS-XREF-OK| WX["4200-CREATE-XREF-EXPORT-RECORD\nSet type 'X'\nMap XREF-CARD-NUM, XREF-CUST-ID, XREF-ACCT-ID\nXREF-ACCT-ID written as COMP binary"]
        WX --> RX
    end

    NEXT_PASS_C --> P5

    subgraph P5 ["Phase 2d — Transaction Pass  ·  5000-EXPORT-TRANSACTIONS"]
        RT["5100-READ-TRANSACTION-RECORD"]
        RT -->|WS-TRANSACTION-EOF| NEXT_PASS_D
        RT -->|WS-TRANSACTION-OK| WT["5200-CREATE-TRAN-EXP-REC\nSet type 'T'\nMap all CVTRA05Y fields\nTRAN-AMT written as COMP-3"]
        WT --> RT
    end

    NEXT_PASS_D --> P6

    subgraph P6 ["Phase 2e — Card Pass  ·  5500-EXPORT-CARDS"]
        RD["5600-READ-CARD-RECORD"]
        RD -->|WS-CARD-EOF| DONE_LOOP
        RD -->|WS-CARD-OK| WD["5700-CREATE-CARD-EXPORT-RECORD\nSet type 'D'\nMap CARD-NUM PAN + CVV-CD\nPCI-DSS concern — plaintext card data"]
        WD --> RD
    end

    DONE_LOOP --> PH3

    subgraph PH3 ["Phase 3 — Finalize  ·  6000-FINALIZE · line 554"]
        CLS["Close all 6 files\nNO error checking on closes"]
        CLS --> STATS["Display export statistics\nCBEXPORT: Export completed\nPer-type counts + total"]
        STATS --> GOBACK(["GOBACK"])
    end

    ABEND(["Display 'CBEXPORT: ABENDING PROGRAM'\nCALL 'CEE3ABD' — NO PARAMETERS\nAbend code uncontrolled\n9999-ABEND-PROGRAM · line 576"])

    classDef startup fill:#EEF4FB,stroke:#2E74B5,color:#000
    classDef loop fill:#FFFDF0,stroke:#ED7D31,color:#000
    classDef shutdown fill:#EEF4FB,stroke:#2E74B5,color:#000
    classDef error fill:#C00000,color:#fff,stroke:none
    classDef write fill:#E2EFDA,stroke:#70AD47,color:#000
    classDef bug fill:#FCE4D6,stroke:#C00000,color:#000

    class INIT,TST,OPEN startup
    class RC,WC,RA,WA,RX,WX,RT,WT,RD loop
    class WD bug
    class CLS,STATS,GOBACK shutdown
    class OPERR,ABEND error
```

---

*Source: `CBEXPORT.cbl`, CardDemo, Apache 2.0 license. Copybooks: `CVCUS01Y.cpy`, `CVACT01Y.cpy`, `CVACT03Y.cpy`, `CVTRA05Y.cpy`, `CVACT02Y.cpy`, `CVEXPORT.cpy`. External service: `CEE3ABD` (IBM Language Environment). All file names, DDnames, paragraph names, field names, PIC clauses, and literal values in this document are taken directly from the source files.*
