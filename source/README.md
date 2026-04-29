# CardDemo COBOL Source

This directory contains COBOL source files extracted from the AWS CardDemo application, an AWS-provided sample mainframe credit card management system.

## Provenance

- **Repository**: https://github.com/aws-samples/aws-mainframe-modernization-carddemo
- **License**: MIT-0 (MIT No Attribution)
- **Description**: A credit card management application originally designed for IBM z/OS mainframes using COBOL, CICS, and VSAM.

## Getting the source

The COBOL files are not committed to git. Clone them manually:

```bash
git clone https://github.com/aws-samples/aws-mainframe-modernization-carddemo.git /tmp/carddemo
cp /tmp/carddemo/app/cbl/*.cbl source/cobol/
cp /tmp/carddemo/app/cbl/*.cpy source/cobol/
cp /tmp/carddemo/app/cpy/*.cpy source/cobol/
```

## Contents

- `*.cbl` / `*.CBL` — 44 COBOL program source files
- `*.cpy` / `*.CPY` — 84 copybook files (shared data definitions)

**Do not modify these files.** They are read-only reference material for the documentation pipeline.
