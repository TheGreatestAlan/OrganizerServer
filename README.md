# nguyen

How to start the nguyen application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/organizerserver-1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8080`

Docker build
---

Use the provided build script:

```
./build/build.sh
```

This builds the `organizer-server` image using a multi-stage Docker build.

Health Check
---

To see your applications health enter url `http://localhost:8081/healthcheck`

## MCP Server

This repository includes an MCP (Model Context Protocol) server that provides Claude Desktop integration for the inventory API.

See [mcp_server/README.md](mcp_server/README.md) for details.

### Quick Start

```bash
# Start the Java server
java -jar target/organizerserver-1.0-SNAPSHOT.jar server config.yml

# In another terminal, run MCP server tests
cd mcp_server
./run_tests.sh
```

### Production (SSE + API Key)

When running in the production container, the MCP server uses SSE transport and
requires a bearer API key on every request.

Required environment variables for the container:

- `MCP_TRANSPORT=sse`
- `MCP_PORT=3000`
- `MCP_API_KEY=...` (required for SSE)
- `INVENTORY_API_BASE_URL=http://localhost:8080`

The container entrypoint starts both the Java server and MCP server and exits
if either process stops.
