# MCP Server Patterns

## Using Existing npm Servers

Most use cases are covered by official MCP servers:

```json
{
  "mcpServers": {
    "server-name": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-<type>", ...additional-args],
      "env": {
        "CREDENTIAL_KEY": "${ENV_VARIABLE}"
      }
    }
  }
}
```

## Custom Server (TypeScript)

For custom integrations, use the MCP SDK:

```typescript
import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";

const server = new Server({
  name: "custom-server",
  version: "1.0.0",
}, {
  capabilities: { tools: {} }
});

server.setRequestHandler(ListToolsRequestSchema, async () => ({
  tools: [{
    name: "query_data",
    description: "Query project data",
    inputSchema: {
      type: "object",
      properties: {
        query: { type: "string", description: "The query to execute" }
      },
      required: ["query"]
    }
  }]
}));

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  if (request.params.name === "query_data") {
    const result = await executeQuery(request.params.arguments.query);
    return { content: [{ type: "text", text: JSON.stringify(result) }] };
  }
});

const transport = new StdioServerTransport();
await server.connect(transport);
```

## Configuration in settings.json

```json
{
  "mcpServers": {
    "custom-server": {
      "command": "node",
      "args": ["path/to/custom-server/index.js"],
      "env": {
        "API_KEY": "${CUSTOM_API_KEY}"
      }
    }
  }
}
```

## Best Practices
1. Use stdio transport (not HTTP) for local servers
2. Validate all input parameters before executing
3. Return structured JSON in tool responses
4. Include error messages in responses, don't throw unhandled exceptions
5. Log all operations for audit purposes
