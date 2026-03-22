# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 0.1.x   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability in EvolutionAI, please report it responsibly.

**Do not open a public GitHub issue for security vulnerabilities.**

Instead, please email: security@scalefirst.ai

Include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

We will acknowledge your report within 48 hours and provide a detailed response within 5 business days.

## Security Considerations

This project handles banking modernization artifacts. Key security principles:

- **No production credentials** should ever be committed to this repository
- **Financial data** in demo artifacts uses synthetic/sample data only
- **Authentication tokens** for Langfuse, Fineract, and other services use default demo values that must be changed in production
- **All generated Java code** undergoes security review by the Witness agent before merge
- **OWASP Top 10** vulnerabilities are checked during the Validate stage
