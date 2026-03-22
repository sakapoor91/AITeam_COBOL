# MCP Server Security Checklist

Complete all items before deploying a new MCP server.

## Credential Management
- [ ] All credentials passed via environment variables (`${VARIABLE}` syntax)
- [ ] No hardcoded tokens, passwords, or connection strings in configuration
- [ ] Credentials stored in `.env` file (must be in `.gitignore`)
- [ ] CI/CD uses encrypted secrets, not plain text

## Access Control
- [ ] Server starts with read-only access
- [ ] Write access added only after read-only validation
- [ ] Scope limited to minimum required resources
- [ ] Path allowlists configured (for filesystem servers)
- [ ] Database access limited to specific schemas/tables (for DB servers)

## Network Security
- [ ] Server communicates over localhost or encrypted channels only
- [ ] No public internet exposure of MCP server ports
- [ ] Connection strings use SSL/TLS where available

## Data Protection
- [ ] No PII exposed through MCP server responses without authorization
- [ ] Query results filtered to exclude sensitive columns
- [ ] Audit logging enabled for all MCP server operations

## Monitoring
- [ ] MCP server health check configured
- [ ] Unusual access patterns trigger alerts
- [ ] Token/credential rotation schedule documented

## Review
- [ ] Security review completed by human (not auto-approved)
- [ ] Witness agent has reviewed the MCP server configuration
- [ ] Configuration documented in `docs/skills-mcp-guide.md`
