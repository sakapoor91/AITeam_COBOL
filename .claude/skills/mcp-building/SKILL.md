---
name: mcp-building
description: MCP server creation patterns and security checklist for tool integration
agent: any
stage: infrastructure
---

# MCP Building Skill

## When to Use
When agents need structured access to an external system and no existing MCP server meets the need.

## Prerequisites
- Clear use case: an agent workflow that repeatedly accesses an external system
- Security review completed (see `references/mcp-security-checklist.md`)

## Steps

1. **Identify the need**: Document which agent, which workflow, what data access is required
2. **Complete security checklist**: Review `references/mcp-security-checklist.md` — all items must pass
3. **Choose server type**:
   - Use existing MCP servers from npm when available (`@modelcontextprotocol/server-*`)
   - Build custom only when no suitable server exists
4. **Configure in settings.json**: Add to `.claude/settings.json` mcpServers section
5. **Start with read-only**: Initial deployment must be read-only
6. **Test in isolation**: Verify server works with a single agent before fleet deployment
7. **Validate**: Run `scripts/validate-mcp-config.sh`
8. **Document**: Add server to the MCP inventory in `docs/skills-mcp-guide.md`

## Available MCP Servers

| Server | Package | Purpose |
|--------|---------|---------|
| github | `@modelcontextprotocol/server-github` | PR management, code review |
| postgres | `@modelcontextprotocol/server-postgres` | Database schema queries |
| filesystem | `@modelcontextprotocol/server-filesystem` | File access with path restrictions |
| langfuse | TBD | Trace ingestion and query (future) |

## Custom Server Pattern
See `references/mcp-server-patterns.md` for implementation guidance.

## References
- `references/mcp-security-checklist.md` — Security review checklist
- `references/mcp-server-patterns.md` — Patterns for custom MCP servers
