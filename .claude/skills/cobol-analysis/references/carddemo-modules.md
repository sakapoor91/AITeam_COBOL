# CardDemo Module Inventory

## Online Modules (CICS)

| Module | Purpose | Complexity | Key Dependencies |
|--------|---------|------------|-----------------|
| COSGN00C | User login/authentication | Moderate | USRSEC dataset, security copybooks |
| COMEN01C | Main menu navigation | Simple | All sub-modules via XCTL |
| COADM01C | Admin menu | Simple | Admin sub-modules |
| COACTUPC | Account update | Complex | ACCTDAT, CUSTDAT datasets |
| COCRDLIC | Credit card list | Moderate | CARDDAT dataset, pagination |
| COCRDUPC | Credit card update | Complex | CARDDAT, ACCTDAT datasets |
| COTRN00C | Transaction processing | Complex | TRANDAT, ACCTDAT, CARDDAT |
| COTRN01C | Transaction detail view | Moderate | TRANDAT dataset |
| COTRN02C | Transaction add | Complex | TRANDAT, ACCTDAT, validation rules |

## Batch Modules

| Module | Purpose | Complexity | Key Dependencies |
|--------|---------|------------|-----------------|
| CBTRN01C | Transaction batch processing | Complex | TRANDAT, ACCTDAT, daily batch rules |
| CBTRN02C | Transaction interest calc | Complex | ACCTDAT, interest rate tables |
| CBTRN03C | Transaction reporting | Moderate | TRANDAT, report formatting |
| CBACT01C | Account batch processing | Complex | ACCTDAT, CUSTDAT, batch rules |
| CBACT02C | Account statement generation | Moderate | ACCTDAT, TRANDAT |
| CBACT03C | Account archival | Simple | ACCTDAT, archive rules |

## Module Naming Convention
- `CO` prefix: Online (CICS) programs
- `CB` prefix: Batch programs
- Suffix letters indicate function area (ACT=Account, CRD=Credit, TRN=Transaction, SGN=Sign-on)
