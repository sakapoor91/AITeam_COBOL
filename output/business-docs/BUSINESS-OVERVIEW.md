# CardDemo — Business Overview

## System Purpose

CardDemo is a complete credit card banking management platform. It allows a bank to manage the full lifecycle of a credit card product: onboarding customers and accounts, issuing cards, authorising purchases in real time, posting daily transactions, calculating monthly interest, generating customer statements, processing bill payments, and producing management reports.

The system runs on IBM mainframe infrastructure. Interactive screens (online functions) are used by bank staff in real time. Batch programs run on a schedule to process large volumes of data overnight or periodically.

---

## Business Domains

### 1. User Security and Access Control
Controls who can log into the system and what they can do. Every staff member must sign in with a user ID and password. Admin users can manage other users' accounts; regular users have access to day-to-day banking operations only.

**Programs:** COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C

---

### 2. Account Management
Manages credit accounts — the financial contracts between the bank and customers. Staff can view account details (balance, credit limit, interest rate, status) and make corrections. Batch programs extract account data and calculate monthly interest charges.

**Programs:** COACTVWC, COACTUPC, COACCT01, CBACT01C, CBACT02C, CBACT04C

---

### 3. Card Management
Manages the physical and logical credit cards issued to customers. Staff can list all cards, view a card's full details (including its CVV and linked account), and update card attributes such as active status and expiry date.

**Programs:** COCRDLIC, COCRDSLC, COCRDUPC, CBACT03C

---

### 4. Transaction Processing
Records and validates all financial activity on accounts. Batch programs run daily to validate incoming transactions against card and account data, post approved transactions to account balances, and produce transaction detail reports. Online screens let staff view and manually enter transactions.

**Programs:** CBTRN01C, CBTRN02C, CBTRN03C, COTRN00C, COTRN01C, COTRN02C, CBCUS01C

---

### 5. Real-Time Authorization
Evaluates incoming card authorization requests in real time via a message queue. Each authorization is approved or declined based on available credit. The system tracks pending (in-flight) authorization holds, displays them to staff for review, supports fraud flagging, and purges expired holds.

**Programs:** COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, CBPAUP0C, DBUNLDGS

---

### 6. Billing and Payments
Allows customers (via bank staff) to make payments on their credit card bill. The screen shows outstanding transactions and processes the payment after explicit confirmation to prevent accidents.

**Programs:** COBIL00C

---

### 7. Statements and Reporting
Generates customer statements (both plain text and HTML) showing transactions, balances, and charges. Also provides on-demand transaction detail reports with date-range filtering, submitted as background batch jobs from an online screen.

**Programs:** CBSTM03A, CBSTM03B, CBACT04C, CBTRN03C, CORPT00C

---

### 8. Reference Data Management
Maintains the lookup tables that give meaning to codes used throughout the system — specifically the list of valid transaction types (e.g., "Purchase", "Cash Advance", "Return"). Managed both through an online admin screen and a batch utility.

**Programs:** COTRTLIC, COTRTUPC, COBTUPDT

---

### 9. Data Migration
Provides the ability to move all customer data (accounts, cards, customers, transactions, cross-references) out of one branch or system and import it into another. Used for branch consolidations or system migrations.

**Programs:** CBEXPORT, CBIMPORT

---

### 10. Administration and Navigation
The admin menu and user menu screens that route staff to the appropriate function. Admins access user management and transaction type maintenance. Regular staff access account, card, transaction, billing, and reporting functions.

**Programs:** COADM01C, COMEN01C

---

### 11. Infrastructure Utilities
Shared services used by other programs: date/time retrieval via messaging, date string validation, job timing/pausing, and the date service used by MQ-based programs.

**Programs:** CODATE01, CSUTLDTC, COBSWAIT

---

## End-to-End Business Flows

### Flow 1 — Staff Login
1. Staff member opens the CardDemo application → **COSGN00C** (sign-on screen)
2. Enters user ID and password; system validates against **USRSEC** file
3. Admin users → **COADM01C** (admin menu)
4. Regular staff → **COMEN01C** (regular user menu)

---

### Flow 2 — View and Update a Customer Account
1. Staff selects "Account" from the main menu → **COACTVWC** (account view screen)
2. Enters an account ID or customer ID; screen shows current balance, credit limit, status
3. Staff presses "Update" → **COACTUPC** (account update screen)
4. Makes changes (phone number, address, status, credit limit, etc.); validates and saves to **ACCTDAT**

---

### Flow 3 — Credit Card Management
1. Staff selects "Cards" from the main menu → **COCRDLIC** (card list screen)
2. Scrolls or filters to find the card; enters `S` to view or `U` to update
3. `S` → **COCRDSLC** (card detail view — read-only)
4. `U` → **COCRDUPC** (card update screen) — changes status, expiry date, or embossed name; saves to **CARDDAT**

---

### Flow 4 — Daily Transaction Batch Processing
1. Transactions accumulate in the **TRANSACT** file from online entries or external feeds
2. **CBTRN01C** runs — validates that each card and account exist (log-only pass)
3. **CBTRN02C** runs — full posting pass: checks card validity, account existence, credit limit, and expiry; approves and posts to account balances; rejects invalid transactions to a reject file with reason codes
4. **CBTRN03C** runs — produces a filtered transaction detail report for a specified date range

---

### Flow 5 — Monthly Interest and Statement Generation
1. **CBACT04C** runs — calculates monthly interest for each account (formula: balance × annual rate ÷ 1200); groups accounts by disclosure group for rate lookups; posts interest transactions and resets cycle totals
2. **CBSTM03A** runs — generates customer statements (plain text and HTML) by loading transaction history and calling **CBSTM03B** for all file I/O; statements branded as "Bank of XYZ"

---

### Flow 6 — Real-Time Card Authorization
1. External system sends authorization request to IBM MQ queue
2. **COPAUA0C** picks up the message — looks up card, account, and customer; checks available credit against pending authorization balance (from IMS)
3. Approves or declines; sends response back on the reply queue; writes authorization record to IMS
4. Staff can review pending authorizations: **COPAUS0C** (list) → **COPAUS1C** (detail; can flag fraud via **COPAUS2C**)
5. **CBPAUP0C** runs periodically in batch to purge expired authorization holds from IMS

---

### Flow 7 — User Account Administration (Admin)
1. Admin logs in → **COADM01C** (admin menu)
2. Selects "Users" → **COUSR00C** (user list)
3. From the list: `U` → **COUSR02C** (update), `D` → **COUSR03C** (delete)
4. To add a new user: directly from admin menu → **COUSR01C** (add user)

---

### Flow 8 — Branch Migration
1. **CBEXPORT** runs on the source system — reads all customers, accounts, cross-references, transactions, and cards; writes a single combined export file with record type codes (C/A/X/T/D); hardcoded to branch `0001`, region `NORTH`
2. Export file is transferred to the target system
3. **CBIMPORT** runs on the target system — reads the combined export file; splits records back into five separate output files by type code; unknown record types go to an error file

---

## Key Data Entities

| Entity | Business Meaning | Primary Storage |
|---|---|---|
| **Customer** | A person or business who holds accounts with the bank — name, address, contact details | CUSTDAT (VSAM) |
| **Account** | A credit account with a credit limit, current balance, interest rate, and status | ACCTDAT (VSAM) |
| **Card** | A credit card issued on an account — card number, expiry, status, embossed name, CVV | CARDDAT (VSAM) |
| **Card-Account Link** | The mapping that connects a card number to its account and customer | CCXREF (VSAM) |
| **Transaction** | A single financial event on a card — purchase, payment, cash advance, etc. | TRANSACT / TRNXFILE (VSAM) |
| **User** | A bank staff member with a login ID, password, and access level | USRSEC (VSAM) |
| **Pending Authorization** | A real-time hold placed on a card's credit for a transaction that is in flight | IMS PAUTSUM0 / PAUTDTL1 |
| **Transaction Type** | A reference code that classifies a transaction (e.g., "Purchase", "Return") | DB2 TRANSACTION_TYPE |
| **Fraud Report** | A record of an authorization that has been flagged as potentially fraudulent | DB2 AUTHFRDS |

---

## Program Inventory

| Program | Domain | Plain-English Purpose |
|---|---|---|
| CBACT01C | Account Management | Validates all account records for data integrity |
| CBACT02C | Account Management | Updates account balances from a sequential input file |
| CBACT03C | Account Management | Copies account records between VSAM files |
| CBACT04C | Billing & Statements | Monthly interest calculation and posting to account balances |
| CBCUS01C | Account Management | Updates customer master records from a sequential input file |
| CBEXPORT | Data Migration | Exports all branch data (customers, accounts, cards, transactions) to a single migration file |
| CBIMPORT | Data Migration | Imports a migration file and splits it back into five separate data files |
| CBPAUP0C | Authorization | Batch purge of expired pending authorization holds from the IMS database |
| CBSTM03A | Statements | Generates customer statements in plain text and HTML format |
| CBSTM03B | Statements | Shared I/O subroutine for CBSTM03A — handles all file reads |
| CBTRN01C | Transaction Processing | Batch validation pass — checks each transaction's card and account exist |
| CBTRN02C | Transaction Processing | Full batch transaction posting — validates, posts balances, rejects failures with codes |
| CBTRN03C | Reporting | Produces a date-range filtered transaction detail report |
| COACCT01 | Account Management | MQ-based account inquiry service — answers account lookup requests via messaging |
| COACTUPC | Account Management | Online account update screen |
| COACTVWC | Account Management | Online account view screen (read-only) |
| COADM01C | Administration | Admin main menu — entry point for admin functions |
| COBIL00C | Billing | Online bill payment processing screen |
| COBSWAIT | Infrastructure | Batch utility — pauses job execution for a specified duration |
| COBTUPDT | Reference Data | Batch maintenance of the transaction type reference table in DB2 |
| COCRDLIC | Card Management | Online card list screen — browse and select cards |
| COCRDSLC | Card Management | Online card detail view screen (read-only) |
| COCRDUPC | Card Management | Online card update screen |
| CODATE01 | Infrastructure | MQ-based date service — answers date requests via messaging |
| COMEN01C | Navigation | Regular user main menu — entry point for daily banking operations |
| COPAUA0C | Authorization | Real-time card authorization decision engine (MQ-triggered) |
| COPAUS0C | Authorization | Online pending authorization summary list screen |
| COPAUS1C | Authorization | Online pending authorization detail view and fraud flagging screen |
| COPAUS2C | Authorization | Backend service — writes fraud reports to DB2 |
| CORPT00C | Reporting | Online report request screen — submits batch report jobs |
| COSGN00C | User Security | Application sign-on screen — authenticates staff and routes by role |
| COTRN00C | Transaction Processing | Online transaction list screen |
| COTRN01C | Transaction Processing | Online transaction detail view screen (read-only) |
| COTRN02C | Transaction Processing | Online manual transaction entry screen |
| COTRTLIC | Reference Data | Online transaction type list — admin maintenance with delete and update |
| COTRTUPC | Reference Data | Online transaction type update screen |
| COUSR00C | User Security | Online user list screen — admin browses all user accounts |
| COUSR01C | User Security | Online add new user screen |
| COUSR02C | User Security | Online update user screen |
| COUSR03C | User Security | Online delete user screen |
| CSUTLDTC | Infrastructure | Shared date validation utility — validates date strings against a format |
| DBUNLDGS | Authorization | Batch IMS database unload — extracts all pending authorization records |

---

## Integration Map (Key Program-to-Program Connections)

### Online Navigation Flow
```
COSGN00C (sign-on)
  ├─ Admin → COADM01C (admin menu)
  │           ├─ COUSR00C → COUSR02C (update user)
  │           │            → COUSR03C (delete user)
  │           ├─ COUSR01C (add user)
  │           └─ COTRTLIC → COTRTUPC (update transaction type)
  └─ Regular → COMEN01C (user menu)
               ├─ COACTVWC → COACTUPC (account view/update)
               ├─ COBIL00C (bill payment)
               ├─ COCRDLIC → COCRDSLC → COCRDUPC (card list/view/update)
               ├─ COTRN00C → COTRN01C (transaction list/view)
               │            → COTRN02C (add transaction)
               ├─ COPAUS0C → COPAUS1C → COPAUS2C (auth list/detail/fraud)
               └─ CORPT00C (report request → submits CBTRN03C batch job)
```

### Shared Data Files (Online ↔ Batch)
| File | Written By | Read By |
|---|---|---|
| ACCTDAT | COACTUPC, CBACT04C | COACTVWC, COACCT01, CBACT01C, CBSTM03A, COPAUA0C, COPAUS0C, CBTRN02C |
| CARDDAT | COCRDUPC | COCRDLC, COCRDSLC, CBACT02C, CBTRN02C, CBEXPORT |
| CCXREF / CXACAIX | COCRDUPC | COCRDLIC, COTRN02C, COBIL00C, COPAUA0C, CBTRN01C, CBTRN02C, CBEXPORT |
| TRANSACT | CBTRN02C, COTRN02C | COTRN00C, COTRN01C, CBTRN03C, CBSTM03A, CBEXPORT, CORPT00C |
| USRSEC | COUSR01C, COUSR02C, COUSR03C | COSGN00C, COUSR00C |
| IMS PAUTSUM0/PAUTDTL1 | COPAUA0C | COPAUS0C, COPAUS1C, CBPAUP0C, DBUNLDGS |
| DB2 TRANSACTION_TYPE | COBTUPDT, COTRTUPC | CBTRN03C, COTRTLIC |
| DB2 AUTHFRDS | COPAUS2C | (fraud audit trail — queried externally) |
