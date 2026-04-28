# AWS CardDemo — Master Reference Document

**Version:** 1.0  
**Date:** 2026-04-28  
**Scope:** All 44 COBOL programs, 62 copybooks, two-phase AI validation complete  
**Source:** CardDemo v2.0 (Apache 2.0 license), IBM mainframe CICS + batch

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [System Architecture](#2-system-architecture)
3. [Business Domains](#3-business-domains)
4. [End-to-End Business Flows](#4-end-to-end-business-flows)
5. [Key Data Entities and Storage](#5-key-data-entities-and-storage)
6. [Copybook Catalog](#6-copybook-catalog)
7. [Complete Program Reference](#7-complete-program-reference)
8. [Validation Summary](#8-validation-summary)
9. [Migration Risk Register](#9-migration-risk-register)
10. [How to Use This Repository](#10-how-to-use-this-repository)

---

## 1. Executive Summary

AWS CardDemo is a complete credit card banking management platform built on IBM mainframe technology. It enables a bank to manage the full lifecycle of a credit card product: onboarding customers and accounts, issuing cards, authorising purchases in real time, posting daily transactions, calculating monthly interest, generating customer statements, processing bill payments, and producing management reports. The system is used exclusively by bank staff — no direct customer-facing interfaces exist. Administrators manage users and reference data; regular staff handle day-to-day account, card, transaction, billing, and reporting operations.

The codebase comprises 44 COBOL programs totalling approximately 35,000 source lines, supported by 62 shared copybooks. The programs run on two IBM platforms: interactive screens (CICS online programs) respond to bank staff in real time, while batch programs run on a JCL schedule to process large data volumes overnight or periodically. External integration includes IBM MQ for real-time card authorisation messaging and date services, IMS hierarchical database for pending authorisation holds, and DB2 relational database for transaction type reference data and fraud reporting. Primary business data — customers, accounts, cards, transactions, and user credentials — is stored in VSAM indexed files.

This modernisation project is translating the entire CardDemo COBOL codebase to Java. The translation process uses a two-phase AI validation pipeline: Phase 1 performs mechanical checks (required sections present, line-number bounds, field name accuracy, byte-count accuracy, copybook coverage), and Phase 2 uses an LLM-as-judge to verify that the business description matches the actual COBOL source. As of the date of this document, all 44 programs have been documented with BIZ-*.md files and validated. Verdicts: 5 PASS, 1 FAIL, 38 CONDITIONAL. The one FAIL is COBIL00C (bill payment screen), which contains material errors in its flow description and must be regenerated before use.

Java developers beginning migration should treat this document as their system orientation. Each program's `BIZ-PROGNAME.md` provides the line-by-line accuracy expected for migration, and each `PROGNAME-validation.md` documents what the validator found wrong. The risk register in Section 9 collects all cross-cutting technical hazards that span multiple programs and require explicit Java design decisions.

---

## 2. System Architecture

### 2.1 Infrastructure Components

| Component | Role in CardDemo |
|-----------|-----------------|
| **IBM CICS** | Online transaction processing. Bank staff interact with BMS-mapped terminal screens. Programs run pseudo-conversationally: each key press invokes the program, which processes input and returns via `EXEC CICS RETURN TRANSID(...)`. State is carried in the COMMAREA (`CARDDEMO-COMMAREA`, defined in `COCOM01Y`). |
| **IBM Batch / JCL** | Scheduled overnight and periodic jobs. Programs are linked as load modules and invoked by JCL job steps with DD cards defining the files. Return codes (e.g., `RETURN-CODE = 4` for rejections in CBTRN02C) flow to JCL `COND` parameters. |
| **VSAM** | Primary file storage for all core entities. KSDS files provide keyed random and sequential access; an ESDS alternate index (`CXACAIX`) provides access to the card cross-reference by account ID. Files are shared between CICS online programs and batch programs. |
| **IBM MQ** | Message queuing for two services: (1) real-time card authorisation — COPAUA0C is MQ-triggered and processes up to 500 authorisation requests per invocation; (2) date service — CODATE01 answers date/time requests from other programs that cannot use `FUNCTION CURRENT-DATE` in certain contexts. |
| **IMS** | Hierarchical database for pending authorisation records. PSB `PSBPAUTB` contains two segment types: `PAUTSUM0` (summary of pending credit per account) and `PAUTDTL1` (individual authorisation detail). COPAUA0C writes to IMS; COPAUS0C, COPAUS1C, CBPAUP0C, and DBUNLDGS read from it. |
| **DB2** | Relational database for two tables: `TRANSACTION_TYPE` (reference data for valid transaction type codes, maintained by COBTUPDT and COTRTUPC, read by CBTRN03C and COTRTLIC) and `AUTHFRDS` (fraud reports written by COPAUS2C and queried externally for audit purposes). |
| **BMS** | Basic Mapping Support — IBM CICS screen definition facility. Each CICS program has a BMS mapset (e.g., `COSGN00`, `COUSR01`) that defines the terminal screen layout. Copybooks named after the mapset (e.g., `COSGN00.cpy`, `COUSR01.cpy`) contain the generated input/output field structures. |

### 2.2 Program Types

**Batch programs** (prefix `CB`, `DB`, `PA`) read and write files through JCL DD card assignments. They open files with explicit `OPEN INPUT/OUTPUT/I-O`, loop through records, and close files on completion. Abend is via IBM Language Environment service `CEE3ABD` (abend code `U999`).

**CICS online programs** (prefix `CO`, `CS`) never open or close files directly. All file I/O goes through `EXEC CICS READ/WRITE/REWRITE/DELETE` commands using the CICS dataset name. Screen interaction uses `EXEC CICS SEND MAP` / `RECEIVE MAP`. Program-to-program transfers use `EXEC CICS XCTL` (non-returning) or `EXEC CICS LINK` (returning). State is passed via `EXEC CICS RETURN ... COMMAREA(...)`.

**Shared utilities** (`CSUTLDTC`, `COBSWAIT`, `CODATE01`) are called by other programs. `CSUTLDTC` validates date strings and is invoked via COBOL `CALL 'CSUTLDTC'`. `COBSWAIT` pauses batch job execution. `CODATE01` is an MQ-triggered date service.

### 2.3 COBOL Standards Used

**COMP-3 (packed decimal)** stores monetary and date values in binary-coded decimal, two digits per byte. When monetary fields are COMP-3, a Java migration must use `java.math.BigDecimal` — never `float` or `double`. Note: not all monetary fields in CardDemo are COMP-3; CVACT01Y account balance fields (`ACCT-CURR-BAL`, `ACCT-CREDIT-LIMIT`, etc.) are zoned decimal with no USAGE clause, which the COBIL00C document incorrectly describes as COMP-3 (see Section 8.2).

**88-level condition names** encode business rules as named boolean flags. Examples: `CDEMO-PGM-REENTER VALUE 1` (re-entry state), `CDEMO-USRTYP-ADMIN VALUE 'A'` (admin user), `PA-AUTH-APPROVED VALUE '00'` (authorization approved), `PA-FRAUD-CONFIRMED VALUE 'F'` (fraud flag). Every 88-level name in the source is a business rule that must be preserved exactly in the Java translation.

**COPY statements** include 62 shared copybooks. All field names, PIC clauses, and 88-level conditions in copybooks must be resolved at migration time — there are no header files or interfaces; copybook content is physically inserted into each program at compile time.

**CICS COMMAREA** (`DFHCOMMAREA` / `CARDDEMO-COMMAREA`) is the inter-program state-passing mechanism for all online programs. Its layout is defined in `COCOM01Y`. Each program receives the full commarea at entry and passes it (possibly modified) to the next program via `EXEC CICS RETURN` or `EXEC CICS XCTL`. In Java, this state machine must be implemented explicitly — the framework will not provide it automatically.

The COMMAREA structure from `COCOM01Y` is:

| Group | Field | PIC | Bytes | Business Meaning |
|-------|-------|-----|-------|-----------------|
| General | `CDEMO-FROM-TRANID` | X(4) | 4 | Transaction ID of calling program |
| General | `CDEMO-FROM-PROGRAM` | X(8) | 8 | Name of calling program |
| General | `CDEMO-TO-TRANID` | X(4) | 4 | Target transaction ID |
| General | `CDEMO-TO-PROGRAM` | X(8) | 8 | Target program name |
| General | `CDEMO-USER-ID` | X(8) | 8 | Authenticated user ID |
| General | `CDEMO-USER-TYPE` | X(1) | 1 | `'A'`=admin, `'U'`=regular user |
| General | `CDEMO-PGM-CONTEXT` | 9(1) | 1 | `0`=first entry, `1`=re-entry |
| Customer | `CDEMO-CUST-ID` | 9(9) | 9 | Customer ID (often zero — not all programs populate) |
| Customer | `CDEMO-CUST-FNAME/MNAME/LNAME` | X(25) each | 75 | Customer name fields |
| Account | `CDEMO-ACCT-ID` | 9(11) | 11 | Account ID |
| Account | `CDEMO-ACCT-STATUS` | X(1) | 1 | Account status flag |
| Card | `CDEMO-CARD-NUM` | 9(16) | 16 | Card number |
| More | `CDEMO-LAST-MAP` | X(7) | 7 | Last BMS map name (for back-navigation) |
| More | `CDEMO-LAST-MAPSET` | X(7) | 7 | Last BMS mapset name |

**Batch error handling** uses a uniform pattern: every file open/read/write/close is followed by a status check. Any non-success status (other than `'00'` and `'10'` for end-of-file on reads) calls a display routine and then `9999-ABEND-PROGRAM`, which invokes the IBM Language Environment service `CEE3ABD` with abend code `999`, producing job completion code `U999`. All programs use the same generic code 999 — there are no program-specific abend codes.

### 2.4 Online Program Navigation Map

All CICS program-to-program transfers use `EXEC CICS XCTL` (non-returning transfer of control). The COMMAREA (`COCOM01Y`) is always passed at transfer. `CDEMO-FROM-PROGRAM` and `CDEMO-FROM-TRANID` record the caller so any program can return to its invoker.

```
COSGN00C (transaction CC00 — sign-on)
  ├─ SEC-USR-TYPE = 'A' (admin) ──→ COADM01C (transaction CADM — admin menu)
  │     ├─ Option 1 ──→ COUSR00C (transaction CU00 — user list)
  │     │     ├─ 'U' action ──→ COUSR02C (transaction CU02 — update user)
  │     │     └─ 'D' action ──→ COUSR03C (transaction CU03 — delete user)
  │     ├─ Option 2 ──→ COUSR01C (transaction CU01 — add user)
  │     ├─ Option 5 ──→ COTRTLIC (transaction TRTL — transaction type list)
  │     │     └─ 'U' action ──→ COTRTUPC (transaction TRUP — type update)
  │     └─ PF3 ──→ COSGN00C
  │
  └─ SEC-USR-TYPE = 'U' (regular) ──→ COMEN01C (transaction CM00 — user menu)
        ├─ Option 1 ──→ COACTVWC (transaction CACV — account view)
        │     └─ PF5/Update ──→ COACTUPC (transaction CAUP — account update)
        │           └─ PF3 ──→ COMEN01C
        ├─ Option 2 ──→ COBIL00C (transaction CB00 — bill payment)
        ├─ Option 3 ──→ COCRDLIC (transaction CCLI — card list)
        │     ├─ 'S' action ──→ COCRDSLC (transaction CCDL — card detail view)
        │     └─ 'U' action ──→ COCRDUPC (transaction CCUP — card update)
        ├─ Option 4 ──→ COTRN00C (transaction CT00 — transaction list)
        │     ├─ 'S' action ──→ COTRN01C (transaction CT01 — transaction detail)
        │     └─ 'A' action ──→ COTRN02C (transaction CT02 — add transaction)
        ├─ Option 5 ──→ COPAUS0C (transaction PAUS — pending auth list)
        │     └─ 'S' action ──→ COPAUS1C (transaction PAU1 — auth detail/fraud flag)
        │           └─ PF5/Fraud ──→ COPAUS2C (transaction PAU2 — write fraud report)
        ├─ Option 6 ──→ CORPT00C (transaction CRPT — report request → submits CBTRN03C)
        └─ PF3 ──→ COSGN00C
```

### 2.5 Shared Data Files — Online and Batch Access

| File / Resource | Written by (online) | Written by (batch) | Read by (online) | Read by (batch) |
|-----------------|--------------------|--------------------|-----------------|----------------|
| `ACCTDAT` (VSAM KSDS) | COACTUPC, COBIL00C | CBACT02C, CBACT04C, CBTRN02C | COACTVWC, COPAUA0C, COPAUS0C | CBACT01C, CBSTM03A, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT |
| `CUSTDAT` (VSAM KSDS) | COACTUPC | CBCUS01C | COACTVWC, COPAUA0C | CBSTM03A, CBTRN03C, CBEXPORT |
| `CARDDAT` (VSAM KSDS) | COCRDUPC | — | COCRDLIC, COCRDSLC | CBACT03C, CBEXPORT |
| `CCXREF` / `CXACAIX` (VSAM KSDS + alt-index) | COCRDUPC | — | COCRDLIC, COBIL00C, COPAUA0C, COTRN02C | CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT |
| `TRANSACT` / `TRNXFILE` (VSAM KSDS) | COTRN02C, COBIL00C | CBTRN02C, CBACT04C | COTRN00C, COTRN01C | CBTRN03C, CBSTM03A, CBEXPORT, CORPT00C |
| `USRSEC` (VSAM KSDS) | COUSR01C, COUSR02C | — | COSGN00C, COUSR00C | — |
| `IMS PAUTSUM0/PAUTDTL1` | COPAUA0C, COPAUS1C | CBPAUP0C | COPAUS0C, COPAUS1C | DBUNLDGS, PAUDBLOD, PAUDBUNL |
| `DB2 TRANSACTION_TYPE` | COTRTUPC | COBTUPDT | COTRTLIC | CBTRN03C |
| `DB2 AUTHFRDS` | COPAUS2C | — | — | (external audit queries) |
| IBM MQ queues | COPAUA0C, CODATE01, COACCT01 | — | COPAUA0C, COACCT01 | — |

---

## 3. Business Domains

### Domain 1 — User Security and Access Control

Controls who can log into the system and what they can do. Every staff member must sign in with a user ID and password. Admin users can manage other users' accounts and reference data; regular users have access to day-to-day banking operations only. The security file `USRSEC` stores user credentials in plain text — a known security weakness documented in the validation findings (see Risk 3 in Section 9).

**Programs:** COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C  
**Primary file:** `USRSEC` (VSAM KSDS)  
**Key copybooks:** `CSUSR01Y` (user record layout `SEC-USER-DATA`), `COCOM01Y` (COMMAREA with `CDEMO-USER-TYPE`, `CDEMO-USER-ID`)

### Domain 2 — Account Management

Manages credit accounts — the financial contracts between the bank and customers. Staff can view account details (balance, credit limit, interest rate, status) and make corrections using a multi-step confirmation screen. Batch programs extract account data, update balances from sequential input, and calculate monthly interest charges.

**Programs:** COACTVWC, COACTUPC, COACCT01, CBACT01C, CBACT02C, CBACT03C, CBACT04C, CBCUS01C  
**Primary files:** `ACCTDAT` (VSAM KSDS), `CUSTDAT` (VSAM KSDS)  
**Key copybooks:** `CVACT01Y` (account record `ACCOUNT-RECORD`), `CUSTREC` (customer record `CUSTOMER-RECORD`), `CVCUS01Y` (customer record variant), `COACTUP` / `COACTVW` (BMS screen maps)

### Domain 3 — Card Management

Manages the physical and logical credit cards issued to customers. Staff can list all cards, view a card's full details (including CVV and linked account), and update card attributes such as active status and expiry date.

**Programs:** COCRDLIC, COCRDSLC, COCRDUPC, CBACT03C  
**Primary file:** `CARDDAT` (VSAM KSDS), `CCXREF` / `CXACAIX` (VSAM KSDS + alternate index)  
**Key copybooks:** `CVCRD01Y` (CICS work area), `CVACT03Y` (card cross-reference record `CARD-XREF-RECORD`), `COCRDLI` / `COCRDSL` / `COCRDUP` (BMS screen maps)

### Domain 4 — Transaction Processing

Records and validates all financial activity on accounts. Batch programs run daily to validate incoming transactions against card and account data, post approved transactions to account balances, and produce transaction detail reports. Online screens let staff view existing transactions and manually enter new ones.

**Programs:** CBTRN01C, CBTRN02C, CBTRN03C, COTRN00C, COTRN01C, COTRN02C, CBCUS01C  
**Primary files:** `DALYTRAN` (sequential input), `TRANSACT` / `TRNXFILE` (VSAM KSDS), `TCATBALF` (VSAM — transaction category balances), `DALYREJS` (sequential reject output)  
**Key copybooks:** `CVTRA05Y` (transaction record `TRAN-RECORD`), `CVTRA06Y` (daily transaction record `DALYTRAN-RECORD`), `CVTRA01Y` (category balance record), `COTRN00` / `COTRN01` / `COTRN02` (BMS screen maps)

### Domain 5 — Real-Time Authorization

Evaluates incoming card authorization requests in real time via IBM MQ. Each authorization is approved or declined based on available credit computed from the IMS pending-authorization summary. The system tracks in-flight authorization holds, displays them to staff for review, supports fraud flagging, and purges expired holds via batch.

**Programs:** COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, CBPAUP0C, DBUNLDGS  
**Primary files:** IMS `PAUTSUM0` / `PAUTDTL1` (via PSB `PSBPAUTB`), IBM MQ queues, DB2 `AUTHFRDS`  
**Key copybooks:** `CIPAUDTY` (IMS pending auth detail segment), `CIPAUSMY` (IMS pending auth summary segment), `CCPAURQY` (MQ request message), `CCPAURLY` (MQ response message), `CCPAUERY` (IMS query structure), `COPAU00` / `COPAU01` (BMS screen maps)

### Domain 6 — Billing and Payments

Allows bank staff to process a full-balance bill payment on behalf of a customer. The screen shows the outstanding balance and processes the payment after explicit operator confirmation, preventing accidental postings.

**Programs:** COBIL00C  
**Primary files:** `ACCTDAT` (VSAM KSDS, read with UPDATE lock), `TRANSACT` (VSAM KSDS, written), `CCXREF` / `CXACAIX`  
**Key copybooks:** `COBIL00` (BMS screen map), `CVACT01Y` (account record), `CVTRA05Y` (transaction record), `CVACT03Y` (card cross-reference)

### Domain 7 — Statements and Reporting

Generates customer statements (both plain text and HTML) showing transactions, balances, and charges. Also provides an on-demand transaction detail report with date-range filtering, submitted as a background batch job from an online screen.

**Programs:** CBSTM03A, CBSTM03B, CBACT04C, CBTRN03C, CORPT00C  
**Primary files:** `TRNXFILE`, `ACCTFILE`, `CUSTFILE`, `XREFFILE`, `STMTFILE` (text output), `HTMLFILE` (HTML output)  
**Key copybooks:** `COSTM01` (transaction record for statements), `CVACT01Y` (account), `CUSTREC` (customer), `CVACT03Y` (cross-reference), `CORPT00` (BMS screen map)

### Domain 8 — Reference Data Management

Maintains the lookup tables that give meaning to transaction type codes used throughout the system (e.g., "Purchase", "Cash Advance", "Return"). Managed both through an online admin screen and a batch utility.

**Programs:** COTRTLIC, COTRTUPC, COBTUPDT  
**Primary storage:** DB2 `TRANSACTION_TYPE` table  
**Key copybooks:** `COTRTLI` / `COTRTUP` (BMS screen maps), `CSDB2RPY` / `CSDB2RWY` (DB2 read/write parameter structures)

### Domain 9 — Data Migration

Provides the ability to move all customer data out of one branch or system and import it into another. Used for branch consolidations or system migrations. The export program is hardcoded to branch `'0001'` and region `'NORTH'`.

**Programs:** CBEXPORT, CBIMPORT  
**Primary files:** `CUSTFILE`, `ACCTFILE`, `XREFFILE`, `TRANSACT`, `CARDFILE` (inputs); `EXPFILE` (export output, indexed VSAM)  
**Key copybooks:** `CVEXPORT` (export record layout `EXPORT-RECORD`)

### Domain 10 — Administration and Navigation

The admin menu and user menu screens that route staff to the appropriate function. Admins access user management and transaction type maintenance. Regular staff access account, card, transaction, billing, and reporting functions.

**Programs:** COADM01C, COMEN01C  
**Key copybooks:** `COADM01` / `COADM02Y` (admin menu structures with OCCURS 9 option table), `COMEN01` / `COMEN02Y` (user menu structures), `COCOM01Y` (COMMAREA navigation fields)

### Domain 11 — Infrastructure Utilities

Shared services used by other programs: date validation, job timing, and the MQ-based date service.

**Programs:** CODATE01, CSUTLDTC, COBSWAIT  
**Key copybooks:** `CSUTLDPY` / `CSUTLDWY` (date validation parameter passing), `CODATECN` (date conversion communication area for `COBDATFT`)

---

## 4. End-to-End Business Flows

### Flow 1 — Staff Login

1. Staff member opens the CardDemo application via transaction `CC00` → **COSGN00C** (sign-on screen, BMS map `COSGN0A`/`COSGN00`)
2. Enters user ID and password; system reads `USRSEC` (VSAM KSDS) using `WS-USER-ID` as key; record layout from `CSUSR01Y` — field `SEC-USR-PWD` compared as plain text
3. If `SEC-USR-TYPE = 'A'` (88-level `CDEMO-USRTYP-ADMIN`): XCTL to **COADM01C** (admin menu)
4. If `SEC-USR-TYPE = 'U'` (88-level `CDEMO-USRTYP-USER`): XCTL to **COMEN01C** (regular user menu)
5. COMMAREA (`COCOM01Y`) carries `CDEMO-USER-ID`, `CDEMO-USER-TYPE`, `CDEMO-FROM-TRANID = 'CC00'` to the next program

**Files:** `USRSEC`  
**Copybooks:** `COCOM01Y`, `COSGN00`, `CSUSR01Y`, `CSMSG01Y`, `CSDAT01Y`, `COTTL01Y`

### Flow 2 — View and Update a Customer Account

1. Staff selects "Account" from the main menu → XCTL to **COACTVWC** (account view, transaction `CACV`, BMS `COACTVW`)
2. Enters account ID; program reads `CARDDAT` cross-reference, then `ACCTDAT` (VSAM KSDS, `CVACT01Y`), then `CUSTDAT` (VSAM KSDS, `CUSTREC`) — displays balance, credit limit, status, customer name
3. Staff presses update → XCTL to **COACTUPC** (account update, transaction `CAUP`, BMS `CACTUPA`/`COACTUP`)
4. Operator modifies fields; COACTUPC validates all inputs, then shows confirmation screen (`ACUP-CHANGES-OK-NOT-CONFIRMED` state in `WS-THIS-PROGCOMMAREA`)
5. PF5 confirms: COACTUPC issues `EXEC CICS READ UPDATE` on both `ACCTDAT` and `CUSTDAT`, checks for concurrent modification (`9700-CHECK-CHANGE-IN-REC`), then issues two `EXEC CICS REWRITE` calls followed by `EXEC CICS SYNCPOINT`

**Files:** `ACCTDAT`, `CUSTDAT`, `CCXREF` / `CXACAIX`  
**Copybooks:** `COCOM01Y`, `CVCRD01Y`, `CVACT01Y`, `CUSTREC`, `CVCUS01Y`, `COACTUP`, `COACTVW`, `CSUTLDWY` (date validation), `CSLKPCDY` (area-code validation)

### Flow 3 — Credit Card Management

1. Staff selects "Cards" from the main menu → XCTL to **COCRDLIC** (card list, transaction `CCLI`, BMS `CCRDLIA`/`COCRDLI`)
2. Program paginates through `CARDDAT` using CICS STARTBR/READNEXT/READPREV/ENDBR; up to 7 cards per page; filter by account number or card number
3. Operator enters `S` beside a card → XCTL to **COCRDSLC** (card detail view, transaction `CCDL`, BMS `CCRDSLAI`/`COCRDSL`) — read-only display of full card record
4. Operator enters `U` beside a card → XCTL to **COCRDUPC** (card update, transaction `CCUP`, BMS `CCRDUPAI`/`COCRDUP`) — changes status, expiry date, or embossed name; writes to `CARDDAT` and updates `CCXREF`

**Files:** `CARDDAT`, `CCXREF`  
**Copybooks:** `COCOM01Y`, `CVCRD01Y`, `CVACT03Y`, `COCRDLI`, `COCRDSL`, `COCRDUP`, `CSSTRPFY` (PF key mapping)

### Flow 4 — Daily Transaction Batch Processing

1. Transactions accumulate in `DALYTRAN` (flat sequential file) from online entries or external feeds
2. **CBTRN01C** runs — log-only validation pass: reads each transaction, looks up card in `XREFFILE` (VSAM, `CVACT03Y`) and account in `ACCTFILE` (VSAM, `CVACT01Y`); logs missing records to job output; no writes to any file
3. **CBTRN02C** runs — full posting pass: for each transaction checks card validity in `XREFFILE`, account status, credit limit, and expiry in `ACCTFILE` (opened I-O); if valid: updates `TCATBALF` (category balances, `CVTRA01Y`), updates account balance in `ACCTFILE`, writes record to `TRANSACT` (`CVTRA05Y`); if invalid: writes to `DALYREJS` with 4-digit reason code; sets `RETURN-CODE = 4` if any rejections
4. **CBTRN03C** runs — date-range filtered report: reads `TRANSACT`, joins with `ACCTFILE` and `CUSTFILE`, writes filtered transaction lines to the report output file

**Files:** `DALYTRAN`, `XREFFILE`, `ACCTFILE`, `TCATBALF`, `TRANSACT`, `DALYREJS`  
**Copybooks:** `CVTRA06Y` (daily tran), `CVACT03Y` (xref), `CVACT01Y` (account), `CVTRA01Y` (category balance), `CVTRA05Y` (posted tran)

### Flow 5 — Monthly Interest and Statement Generation

1. **CBACT04C** runs — for each account in `ACCTFILE`: computes interest as `ACCT-CURR-BAL × ACCT-INTEREST-ID ÷ 1200`; groups accounts by `ACCT-GROUP-ID` for rate lookups from `DISCGRP` (interest rate reference file); writes interest transaction to `TRANSACT`; resets `ACCT-CURR-CYC-CREDIT` and `ACCT-CURR-CYC-DEBIT` to zero; rewrites account record
2. **CBSTM03A** runs — for each card/customer pair in `XREFFILE`: reads customer from `CUSTFILE`, account from `ACCTFILE`, transactions from `TRNXFILE`; delegates all file I/O to subroutine **CBSTM03B** (CALL); writes one plain-text statement to `STMTFILE` and one HTML statement to `HTMLFILE`; bank name hardcoded as `'Bank of XYZ'`

**Files:** `ACCTFILE`, `DISCGRP`, `TRANSACT`, `XREFFILE`, `CUSTFILE`, `TRNXFILE`, `STMTFILE`, `HTMLFILE`  
**Copybooks:** `CVACT01Y`, `CVACT03Y`, `CUSTREC`, `COSTM01`

### Flow 6 — Real-Time Card Authorization

1. External system places authorization request message (18 comma-delimited fields) on IBM MQ queue; CICS trigger fires **COPAUA0C**
2. COPAUA0C reads up to 500 messages per invocation; for each: parses CSV into `PENDING-AUTH-REQUEST` (`CCPAURQY`); looks up `CCXREF` (CICS dataset), `ACCTDAT` (CICS dataset), `CUSTDAT` (CICS dataset) all read-only; reads IMS `PAUTSUM0` segment for available credit
3. Decision: if transaction amount > (`ACCT-CREDIT-LIMIT` − `PA-CREDIT-BALANCE`): decline with `INSUFFICIENT-FUND`; else approve; writes response to MQ reply queue (`CCPAURLY`); writes/updates IMS `PAUTSUM0` and inserts IMS `PAUTDTL1`; issues `EXEC CICS SYNCPOINT` per message
4. Staff can review pending authorizations: **COPAUS0C** (list, BMS `COPAU00`) → **COPAUS1C** (detail/fraud flag, BMS `COPAU01`) → **COPAUS2C** (writes fraud record to DB2 `AUTHFRDS`)
5. **CBPAUP0C** runs periodically in batch: reads IMS, purges authorization holds older than a configured threshold
6. **DBUNLDGS** runs in batch: unloads all IMS authorization records to a flat file for archiving

**Files:** `CCXREF`, `ACCTDAT`, `CUSTDAT` (read-only CICS); IMS `PAUTSUM0`/`PAUTDTL1`; MQ queues; DB2 `AUTHFRDS`  
**Copybooks:** `CCPAURQY`, `CCPAURLY`, `CCPAUERY`, `CIPAUDTY`, `CIPAUSMY`, `PADFLPCB`, `PASFLPCB`, `PAUTBPCB`, `COPAU00`, `COPAU01`

### Flow 7 — User Account Administration

1. Admin logs in → **COADM01C** (admin menu, transaction `CADM`, BMS `COADM1A`/`COADM01`); menu driven by `COADM02Y` option table (6 active options out of OCCURS 9 capacity)
2. Option 1 → XCTL to **COUSR00C** (user list, BMS `COUSR0A`/`COUSR00`): browse all users in `USRSEC`; enter `U` → **COUSR02C** (update user); enter `D` → **COUSR03C** (delete user)
3. Option 2 → XCTL to **COUSR01C** (add user, BMS `COUSR1A`/`COUSR01`): validates 5 fields, writes new record to `USRSEC` via `EXEC CICS WRITE`; password stored as plain 8-char text field `SEC-USR-PWD`
4. Options 5 and 6 → **COTRTLIC** (transaction type list) → **COTRTUPC** (transaction type update): maintenance of DB2 `TRANSACTION_TYPE`

**Files:** `USRSEC`, DB2 `TRANSACTION_TYPE`  
**Copybooks:** `CSUSR01Y`, `COCOM01Y`, `COADM01`, `COADM02Y`, `COUSR00`, `COUSR01`, `COUSR02`, `COUSR03`, `COTRTLI`, `COTRTUP`

### Flow 8 — Branch Migration

1. **CBEXPORT** runs on the source system: reads five files sequentially (`CUSTFILE`, `ACCTFILE`, `XREFFILE`, `TRANSACT`, `CARDFILE`); for each record assembles a 500-byte `EXPORT-RECORD` (`CVEXPORT`) with type code (`C`/`A`/`X`/`T`/`D`), hardcoded `EXPORT-BRANCH-ID = '0001'` and `EXPORT-REGION-CODE = 'NORTH'`; writes to indexed `EXPFILE`; displays per-type counts at end
2. Export file is transferred to target system
3. **CBIMPORT** runs on the target system: reads `EXPFILE` sequentially; routes each record to one of five output files by type code; unrecognised type codes go to an error file; displays per-type import counts

**Files:** `CUSTFILE`, `ACCTFILE`, `XREFFILE`, `TRANSACT`, `CARDFILE`, `EXPFILE`  
**Copybooks:** `CVEXPORT`, `CVACT01Y`, `CUSTREC`, `CVACT03Y`, `CVTRA05Y`

---

## 5. Key Data Entities and Storage

| Entity | Business Meaning | Primary Storage | Record Layout Copybook | Written By | Read By |
|--------|-----------------|-----------------|------------------------|------------|---------|
| **Customer** | A person or business holding accounts — name, address, SSN, FICO score, contact details | `CUSTDAT` (VSAM KSDS, 500 bytes) | `CUSTREC` (`CUSTOMER-RECORD`) | COACTUPC, CBCUS01C | COACTVWC, CBSTM03A (via CBSTM03B), COPAUA0C, CBEXPORT |
| **Account** | A credit account with credit limit, current balance, interest rate, status, and cycle totals | `ACCTDAT` (VSAM KSDS, 300 bytes) | `CVACT01Y` (`ACCOUNT-RECORD`) | COACTUPC, CBACT02C, CBACT04C, CBTRN02C, COBIL00C | COACTVWC, COACCT01, CBACT01C, CBSTM03A, COPAUA0C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT |
| **Card** | A credit card linked to an account — card number, expiry, active status, embossed name, CVV | `CARDDAT` (VSAM KSDS) | `CVACT02Y` (card record) | COCRDUPC | COCRDLIC, COCRDSLC, CBACT03C, CBTRN02C, CBEXPORT |
| **Card-Account Link** | Maps card number to account ID and customer ID | `CCXREF` (VSAM KSDS, 50 bytes) + `CXACAIX` (alternate index by account ID) | `CVACT03Y` (`CARD-XREF-RECORD`) | COCRDUPC | COCRDLIC, COBIL00C, COPAUA0C, COTRN02C, CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT |
| **Transaction** | A single financial event — type, category, amount, merchant, card, timestamps | `TRANSACT` / `TRNXFILE` (VSAM KSDS, 350 bytes) | `CVTRA05Y` (`TRAN-RECORD`) | CBTRN02C, COTRN02C, COBIL00C, CBACT04C | COTRN00C, COTRN01C, CBTRN03C, CBSTM03A, CBEXPORT, CORPT00C |
| **User** | A bank staff member with login ID, password, and access level | `USRSEC` (VSAM KSDS, 80 bytes) | `CSUSR01Y` (`SEC-USER-DATA`) | COUSR01C, COUSR02C | COSGN00C, COUSR00C, COUSR03C (delete) |
| **Pending Authorization** | A real-time hold placed on credit for an in-flight transaction | IMS `PAUTSUM0` (summary) / `PAUTDTL1` (detail) | `CIPAUSMY` / `CIPAUDTY` | COPAUA0C | COPAUS0C, COPAUS1C, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL |
| **Transaction Type** | Reference code classifying a transaction (e.g., "Purchase", "Return") | DB2 `TRANSACTION_TYPE` | `CSDB2RPY` / `CSDB2RWY` (DB2 parameters) | COBTUPDT, COTRTUPC | CBTRN03C, COTRTLIC |
| **Fraud Report** | Record of an authorization flagged as potentially fraudulent | DB2 `AUTHFRDS` | (inline DB2 host variables in COPAUS2C) | COPAUS2C | (queried externally for audit) |
| **Category Balance** | Running totals of transaction amounts by category, per account | `TCATBALF` (VSAM KSDS) | `CVTRA01Y` (`TRAN-CAT-BAL-RECORD`) | CBTRN02C | CBTRN02C (read-update), CBTRN03C |
| **Interest Rate** | Rate lookup by account disclosure group | `DISCGRP` (VSAM KSDS) | `CVACT04Y` (rate record) | (external setup) | CBACT04C |

---

## 6. Copybook Catalog

The 62 copybooks fall into five functional types. `BMS_MAP` copybooks are generated from BMS macros and define CICS screen input/output structures. `DATA_RECORD` copybooks define the on-disk layout of VSAM or sequential file records. `WORKING_STORAGE` copybooks provide shared working-storage fields. `COMMAREA` copybooks define inter-program communication areas. `PCB` copybooks define IMS Program Communication Blocks.

| Copybook | Type | Purpose | Key Users |
|----------|------|---------|-----------|
| `COCOM01Y` | COMMAREA | `CARDDEMO-COMMAREA` — the universal CICS inter-program state container. Contains navigation fields (`CDEMO-FROM-PROGRAM`, `CDEMO-TO-PROGRAM`, `CDEMO-FROM-TRANID`), user context (`CDEMO-USER-ID`, `CDEMO-USER-TYPE`), customer/account/card IDs, and program context flag (ENTER=0/REENTER=1). | All CICS online programs |
| `CVACT01Y` | DATA_RECORD | `ACCOUNT-RECORD` — 300-byte account master record. Key fields: `ACCT-ID` 9(11), `ACCT-CURR-BAL` S9(10)V99, `ACCT-CREDIT-LIMIT` S9(10)V99, `ACCT-ACTIVE-STATUS` X(1), `ACCT-GROUP-ID` X(10). Note: monetary fields are zoned decimal, not COMP-3. Note: `ACCT-EXPIRAION-DATE` is a misspelling preserved from source. | CBACT01C, CBACT04C, COACTUPC, COACTVWC, CBTRN02C, COBIL00C, CBSTM03A, COPAUA0C |
| `CUSTREC` | DATA_RECORD | `CUSTOMER-RECORD` — 500-byte customer master record. Key fields: `CUST-ID` 9(09), `CUST-FIRST-NAME` X(25), `CUST-LAST-NAME` X(25), `CUST-SSN` 9(09), `CUST-FICO-CREDIT-SCORE` 9(03). | COACTUPC, COACTVWC, CBSTM03A, CBCUS01C |
| `CVCUS01Y` | DATA_RECORD | `CUSTOMER-RECORD` variant — alternate working-storage layout for customer data used in interactive programs. | COACTUPC, CBEXPORT |
| `CVACT03Y` | DATA_RECORD | `CARD-XREF-RECORD` — 50-byte card-to-account cross-reference. Fields: `XREF-CARD-NUM` X(16), `XREF-CUST-ID` 9(09), `XREF-ACCT-ID` 9(11), FILLER X(14). | CBTRN01C, CBTRN02C, COBIL00C, COPAUA0C, CBSTM03A |
| `CVTRA05Y` | DATA_RECORD | `TRAN-RECORD` — 350-byte permanent transaction record. Key fields: `TRAN-ID` X(16), `TRAN-TYPE-CD` X(2), `TRAN-AMT` S9(09)V99 (zoned decimal), `TRAN-CARD-NUM` X(16), `TRAN-ORIG-TS` X(26), `TRAN-PROC-TS` X(26). | CBTRN02C, COTRN01C, CBSTM03A |
| `CVTRA06Y` | DATA_RECORD | `DALYTRAN-RECORD` — 350-byte daily transaction input record. Same structure as CVTRA05Y, for the daily input file before posting. | CBTRN01C, CBTRN02C |
| `CSUSR01Y` | DATA_RECORD | `SEC-USER-DATA` — 80-byte user security record. Fields: `SEC-USR-ID` X(8), `SEC-USR-FNAME` X(20), `SEC-USR-LNAME` X(20), `SEC-USR-PWD` X(8) (plain text — security risk), `SEC-USR-TYPE` X(1) (`'A'`=admin, `'U'`=user), FILLER X(23). | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| `CIPAUDTY` | DATA_RECORD | IMS pending authorisation detail segment (`PAUTDTL1`). Contains COMP-3 key fields (`PA-AUTH-DATE-9C`, `PA-AUTH-TIME-9C`), transaction amount (`PA-TRANSACTION-AMT` S9(10)V99 COMP-3), card number, auth type, merchant details, status codes (88-levels: `PA-MATCH-PENDING`, `PA-FRAUD-CONFIRMED`). Byte-size errors documented in validation reports for DBUNLDGS, PAUDBLOD, PAUDBUNL. | COPAUA0C, COPAUS1C, CBPAUP0C, DBUNLDGS |
| `CIPAUSMY` | DATA_RECORD | IMS pending authorisation summary segment (`PAUTSUM0`). Contains per-account credit balance of in-flight holds. | COPAUA0C, COPAUS0C |
| `CVTRA01Y` | DATA_RECORD | `TRAN-CAT-BAL-RECORD` — transaction category balance record for `TCATBALF`. | CBTRN02C |
| `CVEXPORT` | DATA_RECORD | `EXPORT-RECORD` — 500-byte export record for branch migration. Contains type code, branch ID, region code, and the full payload of each entity type. | CBEXPORT, CBIMPORT |
| `CVACT02Y` | DATA_RECORD | Card master record layout for `CARDDAT`. | COCRDUPC, COCRDLIC, COCRDSLC, CBACT03C |
| `CVTRA02Y`, `CVTRA03Y`, `CVTRA04Y`, `CVTRA07Y` | DATA_RECORD | Additional transaction-related record layouts for various batch programs. | CBTRN01C, CBTRN02C, CBTRN03C |
| `CVCRD01Y` | WORKING_STORAGE | `CC-WORK-AREAS` — shared CICS work area containing PF key state (`CCARD-AID` with 88-levels `CCARD-AID-ENTER`, `CCARD-AID-PFK01`–`CCARD-AID-PFK12`), navigation fields (`CCARD-NEXT-PROG`, `CCARD-NEXT-MAP`), account/card/customer ID holders (`CC-ACCT-ID`, `CC-CARD-NUM`, `CC-CUST-ID`), and error message fields. | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C |
| `COADM02Y` | WORKING_STORAGE | `CARDDEMO-ADMIN-MENU-OPTIONS` — admin menu option table. `CDEMO-ADMIN-OPT-COUNT PIC 9(2) VALUE 6` (6 active options); `CDEMO-ADMIN-OPT OCCURS 9 TIMES` (declared for 9 but only 6 used — three slots are dead). Each occurrence has `CDEMO-ADMIN-OPT-NUM(I)` PIC 9(2), `CDEMO-ADMIN-OPT-NAME(I)` X(35), `CDEMO-ADMIN-OPT-PGMNAME(I)` X(8). | COADM01C |
| `COMEN02Y` | WORKING_STORAGE | User main menu option definitions. | COMEN01C |
| `COCOM01Y` | COMMAREA | See entry above. | All CICS programs |
| `COSGN00` | BMS_MAP | BMS mapset `COSGN00`, map `COSGN0A`. Sign-on screen with user ID (`USERIDI` X(8)), password (`PASSWDI` X(8)), error message (`ERRMSGI` X(78)). | COSGN00C |
| `COUSR00`, `COUSR01`, `COUSR02`, `COUSR03` | BMS_MAP | BMS mapsets for user list, add user, update user, delete user screens respectively. | COUSR00C–COUSR03C |
| `COACTUP` | BMS_MAP | BMS mapset `COACTUP`, map `CACTUPA`. Account update screen — 20+ editable fields for account and customer data. | COACTUPC |
| `COACTVW` | BMS_MAP | BMS mapset for account view screen (read-only). | COACTVWC |
| `COADM01` | BMS_MAP | BMS mapset for admin main menu screen. | COADM01C |
| `COMEN01` | BMS_MAP | BMS mapset for regular user main menu. | COMEN01C |
| `COBIL00` | BMS_MAP | BMS mapset `COBIL00`, map `COBIL0A`. Bill payment screen — account ID input, balance display, confirm field. | COBIL00C |
| `COCRDLI`, `COCRDSL`, `COCRDUP` | BMS_MAP | BMS mapsets for card list, card detail, card update screens. | COCRDLIC, COCRDSLC, COCRDUPC |
| `COTRN00`, `COTRN01`, `COTRN02` | BMS_MAP | BMS mapsets for transaction list, transaction detail, add transaction screens. | COTRN00C, COTRN01C, COTRN02C |
| `COTRTLI`, `COTRTUP` | BMS_MAP | BMS mapsets for transaction type list and update screens. | COTRTLIC, COTRTUPC |
| `COPAU00`, `COPAU01` | BMS_MAP | BMS mapsets for pending authorisation list and detail screens. | COPAUS0C, COPAUS1C |
| `CORPT00` | BMS_MAP | BMS mapset for report request screen. | CORPT00C |
| `CCPAURQY` | DATA_RECORD | `PENDING-AUTH-REQUEST` — MQ authorization request message structure (18 CSV fields). | COPAUA0C |
| `CCPAURLY` | DATA_RECORD | `PENDING-AUTH-RESPONSE` — MQ authorization response message structure. | COPAUA0C |
| `CCPAUERY` | WORKING_STORAGE | IMS authorization query structure. | COPAUA0C, COPAUS0C |
| `CSDAT01Y` | WORKING_STORAGE | `WS-DATE-TIME` — current date/time working storage. Sub-fields for year, month, day, hours, minutes, seconds populated by `FUNCTION CURRENT-DATE` or CICS ASKTIME/FORMATTIME. | Most CICS online programs |
| `CSMSG01Y` | WORKING_STORAGE | `CCDA-COMMON-MESSAGES` — shared display messages: `CCDA-MSG-THANK-YOU` X(50), `CCDA-MSG-INVALID-KEY` X(50). | COSGN00C, COADM01C, COMEN01C and others |
| `CSMSG02Y` | WORKING_STORAGE | Additional shared messages. | Multiple programs |
| `COTTL01Y` | WORKING_STORAGE | `CCDA-SCREEN-TITLE` — screen title lines: `CCDA-TITLE01 = 'AWS Mainframe Modernization'`, `CCDA-TITLE02 = 'CardDemo'`. | All CICS programs with screen headers |
| `CSSTRPFY` | WORKING_STORAGE | PF key translation logic — maps CICS `EIBAID` raw attention identifier values to `CCARD-AID` five-character names (e.g., `'PFK01'` through `'PFK12'`). Included as inline code via COPY. | COCRDLIC, COCRDUPC, COTRN00C and others |
| `CSUTLDPY`, `CSUTLDWY` | WORKING_STORAGE | Parameter structures for the `CSUTLDTC` date validation utility. `CSUTLDWY` provides `CSUTLDTC-DATE`, `CSUTLDTC-DATE-FORMAT`, `CSUTLDTC-RESULT`, `CSUTLDTC-RESULT-MSG-NUM`. | COACTUPC, COTRN02C, CSUTLDTC |
| `CSLKPCDY` | WORKING_STORAGE | Area-code lookup table for phone number validation. | COACTUPC |
| `CSDB2RPY`, `CSDB2RWY` | WORKING_STORAGE | DB2 parameter structures for read and write operations against `TRANSACTION_TYPE`. | COTRTLIC, COTRTUPC, COBTUPDT |
| `CSSETATY` | WORKING_STORAGE | Screen attribute settings for BMS field protection/display control. | Multiple CICS programs |
| `CODATECN` | WORKING_STORAGE | `CODATECN-REC` — communication area for assembler date-conversion routine `COBDATFT`. Input: `CODATECN-INP-DATE` X(20), `CODATECN-TYPE` X(1). Output: `CODATECN-0UT-DATE` X(20) (note: zero not letter O — typo in source). Used only by CBACT01C. | CBACT01C |
| `COSTM01` | DATA_RECORD | Transaction record layout for statement generation (`TRNXFILE`). | CBSTM03A, CBSTM03B |
| `PADFLPCB`, `PASFLPCB`, `PAUTBPCB` | PCB | IMS Program Communication Blocks for the authorization subsystem. `PAUTBPCB` is the main PCB for `PSBPAUTB`. | COPAUA0C, COPAUS0C, COPAUS1C, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL |
| `IMSFUNCS` | WORKING_STORAGE | IMS function code constants (GU, GN, GHU, ISRT, REPL, DLET, etc.) for use in IMS DL/I calls. | COPAUA0C, COPAUS0C, COPAUS1C, CBPAUP0C |
| `CVTRA07Y` | DATA_RECORD | Transaction record layout variant used by batch report programs. | CBTRN03C, CORPT00C |
| `UNUSED1Y` | WORKING_STORAGE | Explicitly named unused copybook — contains no active fields. Included for completeness; generates no referenced symbols. | None active |
| `CSMSG02Y`, `CSSETATY`, `COCRDUP`, `CVCRD01Y` (see above) | BMS_MAP / WORKING_STORAGE | Additional shared structures. All documented in individual BIZ-*.md files. | Various |

---

## 7. Complete Program Reference

| Program | Type | Domain | Lines | Primary Files | Key Copybooks | External Calls | Validation |
|---------|------|--------|-------|--------------|---------------|----------------|------------|
| CBACT01C | Batch | Account Management | 430 | ACCTFILE (in), OUTFILE, ARRYFILE, VBRCFILE (out) | CVACT01Y, CODATECN | COBDATFT (assembler), CEE3ABD | CONDITIONAL |
| CBACT02C | Batch | Account Management | 178 | ACCTFILE (I-O), sequential update input | CVACT01Y | CEE3ABD | CONDITIONAL |
| CBACT03C | Batch | Card Management | 178 | CARDFILE (in), CARDCOPY (out) | CVACT02Y | CEE3ABD | PASS |
| CBACT04C | Batch | Statements | 652 | ACCTFILE (I-O), DISCGRP (in), TRANSACT (out) | CVACT01Y, CVTRA05Y | CEE3ABD | CONDITIONAL |
| CBCUS01C | Batch | Account Management | 178 | CUSTFILE (I-O), sequential update input | CUSTREC | CEE3ABD | CONDITIONAL |
| CBEXPORT | Batch | Data Migration | 582 | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE (in); EXPFILE (out) | CVEXPORT, CVACT01Y, CUSTREC, CVACT03Y, CVTRA05Y | CEE3ABD | CONDITIONAL |
| CBIMPORT | Batch | Data Migration | 487 | EXPFILE (in); CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE, error file (out) | CVEXPORT, CVACT01Y, CUSTREC | CEE3ABD | CONDITIONAL |
| CBPAUP0C | Batch | Authorization | 386 | IMS PAUTSUM0/PAUTDTL1 | CIPAUDTY, CIPAUSMY, PADFLPCB, IMSFUNCS | CEE3ABD | CONDITIONAL |
| CBSTM03A | Batch | Statements | 924 | STMTFILE, HTMLFILE (out); delegates I/O to CBSTM03B | CVACT01Y, CUSTREC, CVACT03Y, COSTM01 | CBSTM03B (CALL) | CONDITIONAL |
| CBSTM03B | Batch | Statements | 230 | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE | COSTM01, CUSTREC, CVACT01Y, CVACT03Y | None | PASS |
| CBTRN01C | Batch | Transaction Processing | 494 | DALYTRAN (in), XREFFILE (in), ACCTFILE (in) | CVTRA06Y, CVACT03Y, CVACT01Y | CEE3ABD | CONDITIONAL |
| CBTRN02C | Batch | Transaction Processing | 731 | DALYTRAN (in), XREFFILE (in), ACCTFILE (I-O), TCATBALF (I-O), TRANSACT (out), DALYREJS (out) | CVTRA06Y, CVACT03Y, CVACT01Y, CVTRA01Y, CVTRA05Y | CEE3ABD | CONDITIONAL |
| CBTRN03C | Batch | Reporting | 649 | TRANSACT (in), ACCTFILE (in), CUSTFILE (in), report output file | CVTRA05Y, CVTRA07Y, CVACT01Y, CUSTREC | CEE3ABD | CONDITIONAL |
| COACCT01 | CICS-Online | Account Management | 620 | ACCTDAT (CICS read) | CVACT01Y, COCOM01Y | IBM MQ (MQGET/MQPUT) | PASS |
| COACTUPC | CICS-Online | Account Management | 4236 | ACCTDAT (READ UPDATE/REWRITE), CUSTDAT (READ UPDATE/REWRITE), CXACAIX (READ) | CVACT01Y, CUSTREC, CVCUS01Y, COACTUP, CVCRD01Y, COCOM01Y, CSUTLDWY, CSLKPCDY | CSUTLDTC (inline via CSUTLDWY) | CONDITIONAL |
| COACTVWC | CICS-Online | Account Management | 941 | ACCTDAT (READ), CUSTDAT (READ), CXACAIX (READ) | CVACT01Y, CUSTREC, COACTVW, COCOM01Y, CVCRD01Y | None | CONDITIONAL |
| COADM01C | CICS-Online | Administration | 288 | None (navigation only) | COADM01, COADM02Y, COCOM01Y, CSDAT01Y, CSMSG01Y, COTTL01Y | XCTL to COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC, COTRTUPC, COSGN00C | CONDITIONAL |
| COBIL00C | CICS-Online | Billing | 572 | ACCTDAT (READ UPDATE/REWRITE), CXACAIX (READ), TRANSACT (STARTBR/READPREV/WRITE) | COBIL00, CVACT01Y, CVACT03Y, CVTRA05Y, COCOM01Y | XCTL to calling program | FAIL |
| COBSWAIT | Batch | Infrastructure | 41 | None | None | None | PASS |
| COBTUPDT | Batch | Reference Data | 237 | DB2 TRANSACTION_TYPE | CSDB2RWY | DB2 (embedded SQL) | PASS |
| COCRDLIC | CICS-Online | Card Management | 1459 | CARDDAT (STARTBR/READNEXT/READPREV/ENDBR) | COCRDLI, CVCRD01Y, CVACT03Y, COCOM01Y, CSSTRPFY, CSDAT01Y | XCTL to COCRDSLC, COCRDUPC, COMEN01C | CONDITIONAL |
| COCRDSLC | CICS-Online | Card Management | 887 | CARDDAT (READ), ACCTDAT (READ), CUSTDAT (READ) | COCRDSL, CVCRD01Y, CVACT01Y, CUSTREC, COCOM01Y | XCTL to COMEN01C, COCRDUPC | CONDITIONAL |
| COCRDUPC | CICS-Online | Card Management | 1560 | CARDDAT (READ UPDATE/REWRITE), CCXREF (READ UPDATE/REWRITE) | COCRDUP, CVCRD01Y, CVACT03Y, COCOM01Y | XCTL to COCRDLIC | CONDITIONAL |
| CODATE01 | CICS-Online | Infrastructure | 524 | None (MQ only) | None (inline MQ structures) | IBM MQ (MQOPEN/MQGET/MQPUT/MQCLOSE) | CONDITIONAL |
| COMEN01C | CICS-Online | Navigation | 308 | None (navigation only) | COMEN01, COMEN02Y, COCOM01Y, CSDAT01Y, CSMSG01Y, COTTL01Y | XCTL to COACTVWC, COBIL00C, COCRDLIC, COTRN00C, COPAUS0C, CORPT00C, COSGN00C | CONDITIONAL |
| COPAUA0C | CICS-Online | Authorization | 1026 | CCXREF (READ), ACCTDAT (READ), CUSTDAT (READ), IMS PAUTSUM0/PAUTDTL1 (GU/REPL/ISRT), MQ queues | CCPAURQY, CCPAURLY, CIPAUDTY, CIPAUSMY, PADFLPCB, PAUTBPCB, IMSFUNCS | MQ API, IMS DL/I | CONDITIONAL |
| COPAUS0C | CICS-Online | Authorization | 1032 | IMS PAUTSUM0 (GN browse), ACCTDAT (READ), CUSTDAT (READ) | CCPAUERY, CIPAUSMY, COPAU00, COCOM01Y | XCTL to COPAUS1C | CONDITIONAL |
| COPAUS1C | CICS-Online | Authorization | 604 | IMS PAUTDTL1 (GHU/REPL), CUSTDAT (READ) | CIPAUDTY, COPAU01, COCOM01Y | XCTL to COPAUS2C, COPAUS0C | CONDITIONAL |
| COPAUS2C | CICS-Online | Authorization | 244 | DB2 AUTHFRDS (INSERT) | COCOM01Y | DB2 (embedded SQL) | CONDITIONAL |
| CORPT00C | CICS-Online | Reporting | 649 | TRANSACT (STARTBR/ENDBR for date-range check), submits CBTRN03C JCL | CORPT00, COCOM01Y, CSDAT01Y | XCTL to COMEN01C; CICS SUBMIT for batch job | CONDITIONAL |
| COSGN00C | CICS-Online | User Security | 260 | USRSEC (CICS READ) | COSGN00, COCOM01Y, CSUSR01Y, CSDAT01Y, CSMSG01Y, COTTL01Y | XCTL to COADM01C or COMEN01C | CONDITIONAL |
| COTRN00C | CICS-Online | Transaction Processing | 699 | TRANSACT (STARTBR/READNEXT/READPREV/ENDBR) | COTRN00, CVCRD01Y, CVTRA05Y, COCOM01Y | XCTL to COTRN01C, COTRN02C | CONDITIONAL |
| COTRN01C | CICS-Online | Transaction Processing | 330 | TRANSACT (READ UPDATE — lock never released if no REWRITE follows) | COTRN01, CVTRA05Y, COCOM01Y | None | CONDITIONAL |
| COTRN02C | CICS-Online | Transaction Processing | 783 | CXACAIX (READ), TRANSACT (STARTBR/READPREV/WRITE) | COTRN02, CVACT03Y, CVTRA05Y, COCOM01Y | CALL CSUTLDTC | CONDITIONAL |
| COTRTLIC | CICS-Online | Reference Data | 2098 | DB2 TRANSACTION_TYPE (SELECT, DELETE) | COTRTLI, COCOM01Y | DB2 (embedded SQL); XCTL to COTRTUPC | CONDITIONAL |
| COTRTUPC | CICS-Online | Reference Data | 1702 | DB2 TRANSACTION_TYPE (SELECT, UPDATE) | COTRTUP, COCOM01Y | DB2 (embedded SQL) | CONDITIONAL |
| COUSR00C | CICS-Online | User Security | 695 | USRSEC (STARTBR/READNEXT/ENDBR) | COUSR00, CSUSR01Y, COCOM01Y | XCTL to COUSR02C, COUSR03C | CONDITIONAL |
| COUSR01C | CICS-Online | User Security | 299 | USRSEC (CICS WRITE) | COUSR01, CSUSR01Y, COCOM01Y | XCTL to COADM01C | CONDITIONAL |
| COUSR02C | CICS-Online | User Security | 414 | USRSEC (READ UPDATE/REWRITE) | COUSR02, CSUSR01Y, COCOM01Y | XCTL to COUSR00C | CONDITIONAL |
| COUSR03C | CICS-Online | User Security | 359 | USRSEC (READ / DELETE) | COUSR03, CSUSR01Y, COCOM01Y | XCTL to COUSR00C | CONDITIONAL |
| CSUTLDTC | Utility | Infrastructure | 157 | None | CSUTLDPY, CSUTLDWY | Called via COBOL CALL by COACTUPC, COTRN02C | CONDITIONAL |
| DBUNLDGS | Batch | Authorization | 366 | IMS PAUTSUM0/PAUTDTL1 (GN sequential browse), flat output file | CIPAUDTY, CIPAUSMY, PADFLPCB, IMSFUNCS | IMS DL/I | CONDITIONAL |
| PAUDBLOD | Batch | Authorization | 369 | IMS PAUTDTL1 (GN browse), flat output file | CIPAUDTY, PADFLPCB, IMSFUNCS | IMS DL/I | CONDITIONAL |
| PAUDBUNL | Batch | Authorization | 317 | IMS PAUTDTL1 (GN browse), flat output file | CIPAUDTY, PADFLPCB, IMSFUNCS | IMS DL/I | CONDITIONAL |

---

## 8. Validation Summary

### 8.1 Overall Results

| Verdict | Count | Programs |
|---------|-------|---------|
| PASS | 5 | CBACT03C, CBSTM03B, COACCT01, COBSWAIT, COBTUPDT |
| FAIL | 1 | COBIL00C |
| CONDITIONAL | 38 | All remaining 38 programs |
| **Total** | **44** | |

PASS means the document passed all Phase 1 mechanical checks and the Phase 2 LLM judge found no material errors. CONDITIONAL means the document has warnings that should be read before use — typically missing line-number citations, undocumented paragraphs, or minor omissions — but no fabricated facts. FAIL means the document contains material errors that would mislead a Java developer implementing the program.

### 8.2 The One FAIL: COBIL00C

The bill payment screen documentation (`output/validation/COBIL00C/COBIL00C-validation.md`) received a FAIL verdict for two material errors confirmed by the Phase 2 LLM judge:

**Error 1 — First-display flow description is wrong.** The document states that on first entry the program calls `READ-CXACAIX-FILE` using the card number from `CDEMO-CARD-NUM` in the COMMAREA. This is incorrect. The actual source (line 122) shows that on first entry the program calls `SEND-BILLPAY-SCREEN` immediately — it does not look up any files on the first display. Furthermore, the CXACAIX read (when it does occur) uses the account ID entered by the operator (`ACTIDINI OF COBIL0AI`), not `CDEMO-CARD-NUM`. A Java developer following the documented flow would implement the payment lookup path incorrectly.

**Error 2 — COMP-3 flag wrong for `ACCT-CURR-BAL`.** The document describes `ACCT-CURR-BAL` in the `CVACT01Y` copybook table as `S9(10)V99 COMP-3`. The actual copybook (`CVACT01Y.cpy`, line 7) has no USAGE clause — this field is zoned decimal, not COMP-3. The same applies to `ACCT-CREDIT-LIMIT`, `ACCT-CASH-CREDIT-LIMIT`, `ACCT-CURR-CYC-CREDIT`, and `ACCT-CURR-CYC-DEBIT`. The migration advice to use BigDecimal is still correct, but the storage format information is wrong and could cause a developer to misinterpret the binary encoding of these fields.

Additionally, Phase 1 found three byte-count errors in COCOM01Y fields: `CDEMO-FROM-PROGRAM` and `CDEMO-TO-PROGRAM` documented as 3 bytes each (correct: 8 bytes), and `CDEMO-PGM-CONTEXT` documented as 0 bytes (correct: 1 byte).

### 8.3 Cross-Cutting Findings from CONDITIONAL Reports

The following issues appear repeatedly across multiple programs and represent systemic documentation gaps or code patterns the migration team must address:

**Missing copybooks in Appendix B.** COACTUPC is missing 12 copybooks from its Appendix B, including `DFHBMSCA` (BMS attribute constants), `DFHAID` (attention identifier constants), `COTTL01Y`, `CSDAT01Y`, `CSMSG01Y`, `CSMSG02Y`, `CSUSR01Y`, `CVACT03Y`, and `CVCRD01Y`. These two IBM-supplied copybooks (`DFHBMSCA`, `DFHAID`) are not in the source tree and are not documented anywhere in the repository. Any program that uses BMS attribute constants or attention identifiers depends on these undocumented copybooks. The same gap appears in COACTVWC, COSGN00C, and other BMS programs.

**CIPAUDTY byte-size errors.** The validation reports for DBUNLDGS, PAUDBLOD, and PAUDBUNL all flag byte-size inconsistencies for fields in the `CIPAUDTY` IMS segment copybook. The COMP-3 fields `PA-AUTH-DATE-9C` (S9(05) COMP-3 = 3 bytes) and `PA-AUTH-TIME-9C` (S9(09) COMP-3 = 5 bytes) and `PA-TRANSACTION-AMT` (S9(10)V99 COMP-3 = 7 bytes) must be sized correctly when the IMS segment is mapped to a Java object.

**Dead code patterns.** COUSR00C contains a dead array initialized but never read. COTRTUPC contains an unreachable `'V'` (view) state in its action EVALUATE — the state is set by dead code and no live path reaches the view branch. COTRTLIC contains unreachable delete confirmation paths. These patterns are harmless in COBOL but must not be faithfully reproduced in Java — the Java migration should implement only the reachable states.

**Concurrent update risks.** COUSR02C and COUSR03C both read USRSEC records without optimistic locking, meaning two administrators could simultaneously update or delete the same user record with the second write silently overwriting the first. COTRN01C issues `EXEC CICS READ UPDATE` on the transaction file but — as a view-only screen — never follows with a REWRITE or UNLOCK, holding the CICS lock for the full duration of the pseudo-conversational wait (seconds to minutes). Java must implement explicit locking or optimistic concurrency for all update paths.

**Security issues.** COUSR01C stores the entered password directly in `SEC-USR-PWD` (PIC X(08)) as plain text with no hashing, salting, or encryption. COSGN00C compares the stored password byte-for-byte with the operator's input. The Java migration must replace this with a secure credential store.

**Copy-paste bugs.** COUSR03C (delete user screen) displays the error message `'Unable to Update...'` when a delete operation fails. This message is copied from the update screen (COUSR02C) and is factually wrong — it should say `'Unable to Delete...'`. The Java migration must correct this message.

**ACCTDAT declared but never read in COTRN02C.** The add-transaction screen declares `WS-ACCTDAT-FILE` and copies `CVACT01Y`, suggesting account validation was planned, but no `EXEC CICS READ` against ACCTDAT exists in the source. The document incorrectly describes ACCTDAT as an active validation step. The Java migration should not implement ACCTDAT validation for this screen unless the team decides to add it as a new capability.

---

## 9. Migration Risk Register

The following risks are drawn from validation findings and represent the most important technical hazards for the Java migration team. Each risk includes the affected programs, its source location, and the recommended Java approach.

---

**Risk 1 — COMP-3 monetary fields must use BigDecimal**

- **Description:** COMP-3 (packed decimal) fields store monetary amounts in IBM binary-coded decimal. This encoding has no equivalent in standard Java primitive types. Using `float` or `double` will produce silent precision loss.
- **Affected programs:** All programs handling monetary amounts — CBACT04C, CBTRN02C, CBSTM03A, COPAUA0C, COBIL00C, and others.
- **Source:** IMS segment fields `PA-TRANSACTION-AMT PIC S9(10)V99 COMP-3` in `CIPAUDTY`; `PA-AUTH-DATE-9C PIC S9(05) COMP-3`. Note: account balance fields in `CVACT01Y` are zoned decimal (no COMP-3) but still require BigDecimal for precision.
- **Java recommendation:** Use `java.math.BigDecimal` for all monetary and fixed-precision decimal fields regardless of whether the source is COMP-3 or zoned decimal. Use `BigDecimal.valueOf()` for construction; never use the `double` constructor.

---

**Risk 2 — COBIL00C documentation must be regenerated before use**

- **Description:** The bill payment screen documentation received a FAIL verdict with two material errors: incorrect first-display flow (files are not read on first display) and incorrect COMP-3 flags for account balance fields. A developer using this document as the sole migration reference will implement the payment lookup incorrectly.
- **Affected programs:** COBIL00C.
- **Source:** `output/validation/COBIL00C/COBIL00C-validation.md`, Section S1 and S4.
- **Java recommendation:** Run `/document COBIL00C` to regenerate the documentation from source before beginning Java translation. Verify the regenerated document passes Phase 2 validation before use. Key design points: CXACAIX is keyed by account ID (from screen input `ACTIDINI`), not card number. ACCTDAT is read with `READ UPDATE` lock. The transaction write and the account balance update are not atomic — a failed REWRITE after a successful WRITE leaves the transaction posted but the balance unchanged.

---

**Risk 3 — Plain-text password storage**

- **Description:** `USRSEC` stores passwords as plain 8-character text in `SEC-USR-PWD`. COSGN00C compares passwords with a direct byte-for-byte `EVALUATE` against the stored value. No hashing, salting, or encryption exists anywhere in the system.
- **Affected programs:** COSGN00C (authentication), COUSR01C (add user), COUSR02C (update user), COUSR03C (delete user — reads record before delete).
- **Source:** `CSUSR01Y.cpy` line 21 (`SEC-USR-PWD PIC X(08)`); COSGN00C line 223 (comparison).
- **Java recommendation:** Replace `USRSEC` with a secure user store that stores bcrypt or Argon2 hashed passwords. The 8-character password limit is an artifact of the field size — the Java system should accept longer passwords. Implement account lockout after repeated failures (the COBOL system has none).

---

**Risk 4 — No concurrent update protection on user records**

- **Description:** COUSR02C reads a user record, presents it for editing, and REWRITEs the modified version. COUSR03C reads and then DELETEs. Neither program uses CICS READ UPDATE, so a second administrator updating the same user record simultaneously will have their changes overwritten by the first administrator's REWRITE, silently.
- **Affected programs:** COUSR02C, COUSR03C. Similar pattern noted for COTRN01C (transaction detail view).
- **Source:** COUSR02C — EXEC CICS READ without UPDATE clause, followed by EXEC CICS REWRITE.
- **Java recommendation:** Use optimistic locking (version timestamp or ETag) on all user records. The REWRITE should check that the record was not modified between the READ and the REWRITE. Alternatively, use pessimistic locking (READ with a lock token) if the UX allows it.

---

**Risk 5 — CICS READ UPDATE without REWRITE (lock not released)**

- **Description:** COTRN01C is a view-only transaction detail screen. It issues `EXEC CICS READ UPDATE` on the transaction record, intending to prevent changes during the display, but never issues a matching REWRITE or UNLOCK. In CICS, a READ UPDATE lock is held for the entire duration of the CICS task — which for a pseudo-conversational program means from when the user presses ENTER until the next key press (seconds to minutes). This effectively locks the transaction record against updates from CBTRN02C or other programs for the entire viewing session.
- **Affected programs:** COTRN01C.
- **Source:** COTRN01C — EXEC CICS READ DATASET(...) UPDATE without subsequent EXEC CICS REWRITE or EXEC CICS UNLOCK.
- **Java recommendation:** For a view-only screen, use a plain read (no lock). If the intent was to prevent concurrent modification during review, use optimistic locking with a version check at save time rather than a live lock during display.

---

**Risk 6 — IMS segment byte sizes in documentation may be wrong**

- **Description:** The validation reports for DBUNLDGS, PAUDBLOD, and PAUDBUNL flag byte-count inconsistencies for `CIPAUDTY` IMS segment fields. The COMP-3 encoding of `PA-AUTH-DATE-9C` (S9(05) COMP-3 = 3 bytes), `PA-AUTH-TIME-9C` (S9(09) COMP-3 = 5 bytes), and `PA-TRANSACTION-AMT` (S9(10)V99 COMP-3 = 7 bytes) must be verified against the actual IMS segment definition before building Java data transfer objects.
- **Affected programs:** DBUNLDGS, PAUDBLOD, PAUDBUNL, CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C.
- **Source:** `CIPAUDTY.cpy` — segment total byte count in the BIZ docs may not match what IMS actually stores. Validate against the IMS DBD (database definition) for `PSBPAUTB`.
- **Java recommendation:** Compute exact byte counts from the COMP-3 formula (⌈(n+1)/2⌉ bytes for S9(n) COMP-3) and verify against the IMS DBD before implementing the Java IMS DAO. Do not trust byte counts in any BIZ document for CIPAUDTY fields without source verification.

---

**Risk 7 — STARTBR GTEQ commented out in COTRN00C**

- **Description:** The transaction list screen (COTRN00C) intended to support a GTEQ (greater-than-or-equal) browse start position, allowing paging to begin at the nearest matching key. The `GTEQ` option is commented out in the source; the active STARTBR uses exact-match positioning only. This means if the desired starting key does not exist in the file, the browse will fail with NOTFND instead of positioning at the next available record.
- **Affected programs:** COTRN00C.
- **Source:** COTRN00C — commented-out `GTEQ` clause on EXEC CICS STARTBR.
- **Java recommendation:** When implementing transaction pagination in Java, use a `>=` comparison for key-range queries rather than exact-match. Replicate the GTEQ intent that was removed from the COBOL source, or document that the Java implementation deliberately restores the originally intended behaviour.

---

**Risk 8 — Dead code and unreachable states**

- **Description:** Several programs contain unreachable code paths that must not be migrated as live features: (1) COTRTUPC has a `'V'` (view) action state in its EVALUATE that is set only by dead code — no live path sets the view flag, so the view branch never executes; (2) COTRTLIC has delete-confirmation paths that cannot be reached from the live navigation flow; (3) COUSR00C contains an array populated at initialisation but never used in any display or validation logic; (4) CBACT01C has a loop guard `IF END-OF-FILE = 'N'` inside a `PERFORM UNTIL END-OF-FILE = 'Y'` — the inner check can never be false.
- **Affected programs:** COTRTUPC, COTRTLIC, COUSR00C, CBACT01C.
- **Java recommendation:** Do not implement unreachable states. Remove dead initialization code. Simplify loop guards to match actual logic. If the team wants to restore any of the dead features (e.g., the view action in COTRTUPC), treat that as a new feature design decision, not a migration fidelity requirement.

---

**Risk 9 — Missing IBM-supplied copybooks (DFHBMSCA, DFHAID, IBM MQ)**

- **Description:** Multiple CICS programs depend on two IBM-supplied copybooks not present in the source tree: `DFHBMSCA` (BMS attribute byte constants for screen field protection and colour) and `DFHAID` (attention identifier constants for PF keys, ENTER, CLEAR, PA1, PA2). IBM MQ copybooks (`CMQV`, `CMQSTV`, etc.) defining the MQ API constants and structures are also not in the repository. All of these are resolved at compile time from the IBM mainframe system libraries.
- **Affected programs:** All CICS programs using BMS (DFHBMSCA, DFHAID); COPAUA0C, CODATE01, COACCT01 (MQ).
- **Java recommendation:** Replace `DFHBMSCA` attribute constants with equivalent Spring MVC or Thymeleaf screen attribute logic — these have no direct Java equivalent. Replace `DFHAID` attention identifiers with HTTP request parameter or REST verb dispatch. Replace IBM MQ COBOL API calls with Java MQ client API (`com.ibm.mq`) or Spring Integration / JMS with IBM MQ provider. Document all `DFHBMSCA` colour and protection attributes used in each BMS map before migrating the screen layer.

---

**Risk 10 — Hardcoded branch/region in CBEXPORT**

- **Description:** CBEXPORT hardcodes `EXPORT-BRANCH-ID = '0001'` and `EXPORT-REGION-CODE = 'NORTH'` as literal values not derived from any input parameter or JCL PARM. Any deployment of this program exports data with these fixed identifiers, regardless of which branch is actually being migrated.
- **Affected programs:** CBEXPORT.
- **Source:** `CBEXPORT.cbl` — `EXPORT-BRANCH-ID` and `EXPORT-REGION-CODE` set as VALUE clauses in working storage.
- **Java recommendation:** Replace these literals with configuration parameters (e.g., Spring `@Value` or environment variables) at the Java migration. Add validation that branch ID and region are provided at runtime and are not empty. Do not carry these values forward as business constants.

---

## 10. How to Use This Repository

This guide is for a Java developer beginning migration of CardDemo programs.

**Step 1 — System orientation.** Read this document in full before touching any individual program. The architecture (Section 2), business flows (Section 4), and risk register (Section 9) provide the context you need to understand how programs fit together and where the known hazards are.

**Step 2 — Read the program document before the source.** For each program you are assigned to migrate, read `output/business-docs/PROGNAME/BIZ-PROGNAME.md` first. These documents describe every data action, field, copybook, and external call in plain English. You should not need to read the COBOL source for most migration decisions.

**Step 3 — Check the validation report before trusting the document.** Before relying on any BIZ document, read `output/validation/PROGNAME/PROGNAME-validation.md`. Look at the Phase 2 verdict (PASS / CONDITIONAL / FAIL) and the findings. For CONDITIONAL programs, read the warnings in S3–S6 to understand which specific claims need extra verification against the source. For a FAIL, do not use the document as a primary reference.

**Step 4 — COBIL00C is a FAIL — read the source directly.** The bill payment screen documentation contains material errors. Until the document is regenerated and re-validated, read `source/cobol/COBIL00C.cbl` directly for migration. The key points are: first entry shows a blank screen; CXACAIX is keyed by account ID (screen input), not card number; ACCTDAT is read with UPDATE lock; the transaction write and balance update are sequential without a SYNCPOINT, creating an atomicity gap.

**Step 5 — Regenerate a document.** If you find errors in a BIZ document, or if you need a document for a program that does not have one yet, run the `/document PROGNAME` slash command in Claude Code. This invokes the documentation generation skill which reads the COBOL source and produces a conformant BIZ-PROGNAME.md.

**Step 6 — Re-validate after edits.** After regenerating or editing any BIZ document, run `/validate-doc PROGNAME` to trigger both Phase 1 and Phase 2 validation. Check that the verdict improves. A CONDITIONAL verdict is acceptable for migration use; a FAIL requires revision before handover.

**Step 7 — Resolve copybooks before coding.** Every field name in a BIZ document references a copybook. Before writing Java data transfer objects or entity classes, locate the copybook in `source/cobol/`, read the actual PIC clause, and compute the byte count yourself. Do not rely on byte counts in BIZ documents — the COADM01C and COBIL00C validation reports both found byte-count errors in Appendix B tables.

**Step 8 — Address the risks in Section 9 at design time.** Risks 1–10 in the migration risk register are not bugs to fix after migration — they are design decisions that must be made before writing code. The most critical are: (1) all monetary fields need BigDecimal; (3) password storage must be replaced; (4) concurrent update protection must be added; (5) COTRN01C READ UPDATE must be changed to a plain read.

---

*Source: AWS CardDemo COBOL codebase, Apache 2.0 license. This document was generated by automated analysis of 44 COBOL source programs and 62 copybooks, supported by two-phase AI validation reports. All program names, file names, field names, copybook names, paragraph names, and literal values cited in this document are taken directly from the source files or from the BIZ-*.md documentation produced from those source files.*
