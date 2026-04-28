# BIZ-COADM01C

| Field | Value |
|---|---|
| **Application** | AWS CardDemo |
| **Source File** | `source/cobol/COADM01C.cbl` |
| **Program Type** | CICS online — administration menu |
| **Transaction ID** | `CA00` |
| **Map / Mapset** | `COADM1A` / `COADM01` |
| **Last Updated** | 2026-04-28 |

---

> **Purpose Banner** — COADM01C is the CardDemo administration main menu. It presents a numbered list of administrative functions to the terminal operator, receives a menu selection, validates it, and transfers control (XCTL) to the selected program. It reads no files whatsoever; all business state comes from the COMMAREA and the hardcoded `COADM02Y` option table.

---

## Section 1 — Business Purpose

COADM01C is the entry point for CardDemo back-office administration. Users with administrative access (`CDEMO-USER-TYPE` = `'A'`) are directed here after sign-on. The program displays up to nine numbered menu options, each linked to a specific administrative program name. The current CardDemo deployment defines six active options:

| Option | Label | Program |
|---|---|---|
| 1 | User List | `COUSR00C` |
| 2 | Add User | `COUSR01C` |
| 3 | Update User | `COUSR02C` |
| 4 | Delete User | `COUSR03C` |
| 5 | Transaction List | `COTRTLIC` |
| 6 | Update Transaction | `COTRTUPC` |

Options 7–9 are allocated in the OCCURS table but are not populated; if accessed they trigger the "not installed" guard. Pressing PF3 returns to the sign-on screen (`COSGN00C`).

---

## Section 2 — Program Flow

COADM01C follows the CICS pseudo-conversational pattern. Each terminal interaction is a separate CICS task. The program uses `CDEMO-PGM-CONTEXT` in the COMMAREA to distinguish first entry (context = `0`) from re-entry (context = `1`).

### 2.1 Startup

1. The program is entered either from a CICS START (transaction `CA00`) or by an XCTL from the sign-on program (`COSGN00C`).
2. On entry, the program checks whether an EIBCALEN > 0 COMMAREA was passed. If no COMMAREA is present (`EIBCALEN = 0`), the program initialises a fresh `CARDDEMO-COMMAREA` in working storage.
3. `SETUP-TERM-ATTR` applies the current terminal's attribute values.
4. `MAIN-PARA` is entered. It checks `CDEMO-PGM-CONTEXT`:
   - If `CDEMO-PGM-CONTEXT = 0` (ENTER — first invocation): calls `SEND-MENU-SCREEN` directly to paint the menu without processing prior input.
   - If `CDEMO-PGM-CONTEXT = 1` (REENTER — user responded): calls `RECEIVE-MENU-SCREEN` to read the terminal input, then decides what to do.

### 2.2 Main Processing

**First time display — `SEND-MENU-SCREEN`:**

1. `BUILD-MENU-OPTIONS` is called to populate the on-screen option lines. It loops from 1 to `CDEMO-ADMIN-OPT-COUNT` (= 6). For each index:
   - Moves the option number, a period, and the option name (`CDEMO-ADMIN-OPT-NAME(I)`) into the corresponding BMS map field `CDMENUxI` (lines 1–10 only; `WHEN OTHER` is `CONTINUE`, so indices above 10 are silently skipped — this is a latent bug since the OCCURS only has 9 entries anyway).
2. The BMS map `COADM1A` is sent to the terminal using `EXEC CICS SEND MAP` with `ERASE`, refreshing the entire screen.
3. `SET-CDEMO-PGM-CONTEXT-REENTER` sets `CDEMO-PGM-CONTEXT = 1`.
4. `EXEC CICS RETURN TRANSID('CA00') COMMAREA(CARDDEMO-COMMAREA)` suspends the task and schedules re-entry under transaction `CA00` when the user presses any key.

**Subsequent entry — `RECEIVE-MENU-SCREEN`:**

1. `EXEC CICS RECEIVE MAP('COADM1A') MAPSET('COADM01')` reads the map input from the terminal into the BMS field `WS-OPTION`.
2. The AID key is stored by the inline equivalent of `YYYY-STORE-PFKEY` (the program uses its own `EVALUATE EIBAID` logic rather than the CSSTRPFY copybook).
3. Key dispatch:
   - **PF3** → `RETURN-TO-SIGNON-SCREEN`: `EXEC CICS XCTL PROGRAM('COSGN00C')` passing the COMMAREA. Control does not return.
   - **ENTER** → `PROCESS-ENTER-KEY`: validates the option number and XCTLs to the chosen program.
   - **Any other key** → `SEND-INVALID-KEY-SCREEN`: sends the standard 'Invalid key pressed' message (`CCDA-MSG-INVALID-KEY`) and returns via `EXEC CICS RETURN TRANSID('CA00')`.

**Option processing — `PROCESS-ENTER-KEY`:**

1. `WS-OPTION` is compared to `ZEROS` or `> CDEMO-ADMIN-OPT-COUNT` (6). If out of range, `POPULATE-HEADER-INFO` is called, the error message 'Please enter a valid option number...' is moved to the screen error field, and `SEND-MENU-SCREEN` repaints the menu.
2. If the option is valid, `CDEMO-ADMIN-OPT-PGMNAME(WS-OPTION)(1:5)` is checked against the string `'DUMMY'`. If it matches, the program moves 'This option is not installed ...' (in green attribute) to the header message field and redisplays the menu.
3. If not a DUMMY, `EXEC CICS XCTL PROGRAM(CDEMO-ADMIN-OPT-PGMNAME(WS-OPTION))` is executed. `PGMIDERR` condition is HANDLEd; if the target program is not found in the CICS Program Definition table, `PGMIDERR-ERR-PARA` fires and displays the same 'not installed' message.

### 2.3 Shutdown / Return

The program never calls `EXEC CICS RETURN` without a `TRANSID` except in the ABEND routine. Normal termination is always either:
- `EXEC CICS RETURN TRANSID('CA00') COMMAREA(...)` — to await the next user interaction, or
- `EXEC CICS XCTL PROGRAM(...)` — to transfer permanently to another program.

`ABEND-ROUTINE` (triggered by `EXEC CICS HANDLE ABEND LABEL(ABEND-ROUTINE)`): populates `ABEND-DATA` with the program name (`COADM01C`) and a reason string, sends the ABEND message to the terminal using `EXEC CICS SEND TEXT`, then issues `EXEC CICS ABEND ABCODE('9999')`.

---

## Section 3 — Error Handling

| Condition | Trigger | Response |
|---|---|---|
| Invalid option (< 1 or > 6) | `WS-OPTION <= ZEROS` or `> CDEMO-ADMIN-OPT-COUNT` | Error message 'Please enter a valid option number...' displayed; menu repainted |
| DUMMY option | `CDEMO-ADMIN-OPT-PGMNAME(opt)(1:5) = 'DUMMY'` | 'This option is not installed ...' displayed in green; menu repainted |
| PGMIDERR | CICS handle condition | Same 'not installed' message; menu repainted |
| Invalid AID key | Any key other than ENTER, PF3 | 'Invalid key pressed. Please see below...' sent; task returns with TRANSID |
| ABEND | Any unexpected CICS ABEND | `ABEND-DATA` populated; text sent; `EXEC CICS ABEND ABCODE('9999')` issued |

---

## Section 4 — Migration Notes

1. **No file I/O whatsoever.** The menu is built entirely from the in-memory `COADM02Y` option table. The Java replacement can use a static configuration list (or a database-driven option table if the menu must be reconfigurable at runtime).

2. **CDEMO-ADMIN-OPT-COUNT is hardcoded to 6 in the copybook.** The `BUILD-MENU-OPTIONS` loop ceiling is the copybook constant, not a configurable value. The Java replacement should externalise this as a configuration property.

3. **Latent bug — `BUILD-MENU-OPTIONS` WHEN OTHER is CONTINUE.** The `EVALUATE` in `BUILD-MENU-OPTIONS` has explicit `WHEN 1 THROUGH 10` cases but `WHEN OTHER CONTINUE`. Since the OCCURS only has 9 entries, an index of 10 would reference beyond the table end on z/OS. However, since `CDEMO-ADMIN-OPT-COUNT = 6`, the loop never reaches 10. The Java replacement should use a bounded loop of exactly `optionCount` iterations with no WHEN OTHER gap.

4. **PF13–PF24 aliased to PF1–PF12 via CSSTRPFY.** The `YYYY-STORE-PFKEY` inline logic maps PF13→PF1, PF14→PF2, etc. The Java/web replacement does not face this constraint; physical function keys are not a concern.

5. **XCTL is a one-way transfer.** `EXEC CICS XCTL` does not preserve a return address. The callee is responsible for navigating back. Java method calls are two-way by default; navigation state must be managed explicitly (e.g., session attribute tracking the back-destination).

6. **COMMAREA carries navigation context.** `CDEMO-FROM-PROGRAM` and `CDEMO-TO-PROGRAM` in the COMMAREA inform the destination program where the user came from. The Java session/HTTP context should carry equivalent navigation metadata.

7. **PGMIDERR is used as a program-not-found guard.** This is a CICS-specific mechanism. In Java, the equivalent is checking whether a Spring bean or REST endpoint exists before redirecting. Use a whitelist-based dispatch table rather than a try/catch equivalent.

8. **Screen attribute colours.** The DUMMY/PGMIDERR message uses a green (`DFHGREEN`) attribute. The Java/web UI should represent this as an informational (not error) style class.

9. **`CDEMO-ADMIN-OPT-COUNT` (= 6) and the OCCURS 9 discrepancy.** Three slots in the OCCURS table are unused but allocated. The Java replacement should only allocate and expose the actual number of active options.

---

## Appendix A — Files and Queues

This program accesses no files, VSAM datasets, databases, or message queues.

---

## Appendix B — Copybooks and External Programs

### Copybooks

**`COCOM01Y`** — `CARDDEMO-COMMAREA` (full CardDemo shared communication area)

| Field | Picture | Notes |
|---|---|---|
| `CDEMO-FROM-TRANID` | `PIC X(4)` | Sending transaction ID |
| `CDEMO-FROM-PROGRAM` | `PIC X(8)` | Sending program name |
| `CDEMO-TO-TRANID` | `PIC X(4)` | Destination transaction |
| `CDEMO-TO-PROGRAM` | `PIC X(8)` | Destination program name |
| `CDEMO-USER-ID` | `PIC X(8)` | Signed-in user ID |
| `CDEMO-USER-TYPE` | `PIC X(1)` | `'A'` = Admin, `'U'` = Regular user |
| `CDEMO-PGM-CONTEXT` | `PIC X(1)` | `'0'` = first entry (ENTER), `'1'` = re-entry (REENTER) |
| `CDEMO-LAST-MAP` | `PIC X(7)` | Last BMS map name sent |
| `CDEMO-LAST-MAPSET` | `PIC X(7)` | Last BMS mapset name sent |
| Customer, account, card sub-groups | Various | **Not used by this program** |

**`COADM02Y`** — `CARDDEMO-ADMIN-MENU-OPTIONS`

| Field | Picture | Value / Notes |
|---|---|---|
| `CDEMO-ADMIN-OPT-COUNT` | `PIC 9(2)` | Value `06` — total active options |
| `CDEMO-ADMIN-OPT` | OCCURS 9 TIMES | Option table |
| `CDEMO-ADMIN-OPT-NUM(I)` | `PIC 9(2)` | Sequential option number (1–6) |
| `CDEMO-ADMIN-OPT-NAME(I)` | `PIC X(35)` | Display label, e.g. `'User List'` |
| `CDEMO-ADMIN-OPT-PGMNAME(I)` | `PIC X(8)` | Target CICS program name, e.g. `'COUSR00C'`; `'DUMMY   '` marks unimplemented slots |

**`COTTL01Y`** — `CCDA-SCREEN-TITLE`

| Field | Value | Notes |
|---|---|---|
| `CCDA-TITLE01` | `'      AWS Mainframe Modernization       '` | Screen title line 1 |
| `CCDA-TITLE02` | `'              CardDemo                  '` | Screen title line 2 |
| `CCDA-THANK-YOU` | Sign-off message | **Not used in this program** |

**`CSDAT01Y`** — `WS-DATE-TIME` date/time working storage; populated by `GET-CURRENT-DATETIME` using CICS `ASKTIME`/`FORMATTIME`. Fields `WS-CURDATE-MM-DD-YY` and `WS-CURTIME-HH-MM-SS` are displayed in the screen header.

**`CSMSG01Y`** — `CCDA-COMMON-MESSAGES`

| Field | Value | Used? |
|---|---|---|
| `CCDA-MSG-THANK-YOU` | Thank-you banner | No |
| `CCDA-MSG-INVALID-KEY` | `'Invalid key pressed. Please see below...'` | Yes — invalid AID response |

**`CSMSG02Y`** — `ABEND-DATA` structure (ABEND-CODE X(4), ABEND-CULPRIT X(8), ABEND-REASON X(50), ABEND-MSG X(72)) — used in `ABEND-ROUTINE` only.

**`CSUSR01Y`** — `SEC-USER-DATA` (SEC-USR-ID, SEC-USR-FNAME, SEC-USR-LNAME, SEC-USR-TYPE, etc.) — **Not used by this program.**

**`DFHAID`** — CICS-supplied AID key constants (`DFHENTER`, `DFHCLEAR`, `DFHPA1`, `DFHPA2`, `DFHPF1`–`DFHPF24`). Used in the AID EVALUATE.

**`DFHBMSCA`** — CICS-supplied BMS screen attribute constants (`DFHBMPRF`, colour codes, etc.). Used for setting field attributes on the menu screen.

**`COADM01`** — BMS mapset copybook for mapset `COADM01`, map `COADM1A`. Provides input/output field names used in the RECEIVE/SEND MAP calls (e.g., `CDMENU1I` through `CDMENU9I` for the nine option display lines, `CDERR1O`/`CDERR2O` for error message lines).

### External Programs Called

| Program | CICS Mechanism | When | Notes |
|---|---|---|---|
| `COSGN00C` | XCTL | PF3 | Return to sign-on; one-way transfer |
| `COUSR00C` | XCTL | Option 1 | User list |
| `COUSR01C` | XCTL | Option 2 | Add user |
| `COUSR02C` | XCTL | Option 3 | Update user |
| `COUSR03C` | XCTL | Option 4 | Delete user |
| `COTRTLIC` | XCTL | Option 5 | Transaction list |
| `COTRTUPC` | XCTL | Option 6 | Update transaction |

---

## Appendix C — Hardcoded Literals

| Location | Literal | Meaning |
|---|---|---|
| `EXEC CICS RETURN TRANSID` | `'CA00'` | Re-entry transaction ID for this program |
| `EXEC CICS XCTL` on PF3 | `'COSGN00C'` | Sign-on program name |
| `EXEC CICS ABEND ABCODE` | `'9999'` | Diagnostic ABEND code |
| `PROCESS-ENTER-KEY` | `'DUMMY'` (5-char check) | Guard string for unimplemented menu options |
| Error message | `'Please enter a valid option number...'` | User-visible validation message |
| DUMMY/PGMIDERR message | `'This option is not installed ...'` | User-visible feature-unavailable message |
| `CDEMO-ADMIN-OPT-COUNT` (in COADM02Y) | `06` | Number of active menu options |

---

## Appendix D — Internal Working Fields

| Field | Picture | Usage | Notes |
|---|---|---|---|
| `WS-OPTION` | `PIC 9(2)` | Receives the user's numeric menu choice from the BMS map | Range validated against 1–`CDEMO-ADMIN-OPT-COUNT` |
| `WS-PGMNAME` | `PIC X(8)` | Holds the resolved CICS program name before XCTL | Set from `CDEMO-ADMIN-OPT-PGMNAME(WS-OPTION)` |
| `CARDDEMO-COMMAREA` | COPY `COCOM01Y` | Full shared navigation COMMAREA | Passed on every RETURN TRANSID and XCTL |
| `ABEND-DATA` | COPY `CSMSG02Y` | ABEND diagnostic structure | Only populated in `ABEND-ROUTINE` |
| `WS-DATE-TIME` | COPY `CSDAT01Y` | Current date/time for screen header | Populated by `GET-CURRENT-DATETIME` |
| `SEC-USER-DATA` | COPY `CSUSR01Y` | User security record | **Declared but not used in this program** |

---

## Appendix E — Control Flow Diagram

```mermaid
flowchart TD
    classDef cicsio fill:#2d4a1e,stroke:#6ab04c,color:#ffffff
    classDef decision fill:#4a3a1e,stroke:#d9a44a,color:#ffffff
    classDef process fill:#1e3a5f,stroke:#4a90d9,color:#ffffff
    classDef error fill:#4a1e1e,stroke:#d94a4a,color:#ffffff
    classDef term fill:#2e1e4a,stroke:#8a4ad9,color:#ffffff

    START([CICS Invocation\nTRANSID CA00]):::cicsio
    COMM{EIBCALEN > 0?}:::decision
    INIT[Init CARDDEMO-COMMAREA]:::process
    CTX{CDEMO-PGM-CONTEXT}:::decision

    subgraph PH1 [First Entry]
        BUILD[BUILD-MENU-OPTIONS\nloop 1 to 6]:::process
        SEND[EXEC CICS SEND MAP COADM1A\nERASE]:::cicsio
        SETCTX[Set CDEMO-PGM-CONTEXT = REENTER]:::process
        RET1[EXEC CICS RETURN TRANSID CA00\nCOMMARREA]:::cicsio
    end

    subgraph PH2 [Re-Entry Processing]
        RECV[EXEC CICS RECEIVE MAP COADM1A]:::cicsio
        AID{EIBAID?}:::decision
        PF3[XCTL COSGN00C]:::cicsio
        INVKEY[Send Invalid Key message\nRETURN TRANSID CA00]:::error
        ENTER[PROCESS-ENTER-KEY]:::process
    end

    subgraph PH3 [Option Dispatch]
        RANGE{WS-OPTION valid\n1–6?}:::decision
        ERRMSG[Show 'valid option' error\nRepaint menu]:::error
        DUMMY{PGMNAME(1:5)\n= 'DUMMY'?}:::decision
        NOTINST[Show 'not installed'\nRepaint menu]:::error
        XCTL[EXEC CICS XCTL\nPROGRAM target]:::cicsio
        PGMERR[PGMIDERR handler\nShow 'not installed']:::error
    end

    ABEND([ABEND-ROUTINE\nSEND ABEND-DATA\nABEND 9999]):::term

    START --> COMM
    COMM -->|No| INIT
    COMM -->|Yes| CTX
    INIT --> CTX
    CTX -->|= 0 ENTER| BUILD
    BUILD --> SEND --> SETCTX --> RET1
    CTX -->|= 1 REENTER| RECV
    RECV --> AID
    AID -->|PF3| PF3
    AID -->|ENTER| ENTER
    AID -->|Other| INVKEY
    ENTER --> RANGE
    RANGE -->|No| ERRMSG
    RANGE -->|Yes| DUMMY
    DUMMY -->|Yes| NOTINST
    DUMMY -->|No| XCTL
    XCTL -->|PGMIDERR| PGMERR
    ERRMSG --> SEND
    NOTINST --> SEND
    PGMERR --> SEND
```
