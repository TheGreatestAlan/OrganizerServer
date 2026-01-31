# MCP Inventory Server

A Model Context Protocol (MCP) server that exposes inventory management operations to Claude Desktop and other MCP clients.

## Overview

This MCP server provides natural language access to the Java Inventory API, allowing users to manage their inventory through conversational interaction with Claude.

## Architecture

```
┌─────────────────┐     MCP Protocol      ┌──────────────┐     REST      ┌──────────────┐
│  Claude Desktop │  ◄──────────────────►  │  MCP Server  │  ◄─────────► │  Java        │
│                 │   tools/list, call    │  (Python)    │              │  Inventory   │
│                 │                       │              │              │  API         │
└─────────────────┘                       └──────────────┘              └──────────────┘
```

## Features

- **7 Registered Tools**: Complete inventory management through MCP protocol
- **Natural Language Interface**: Conversational interaction through Claude Desktop
- **Direct API Integration**: No intermediate LLM parsing (lower latency, lower cost)
- **Dry-Run Mode**: Preview changes before executing mutating operations
- **Stateless Design**: Simpler architecture than WebSocket-based predecessor
- **Transport Options**: Supports both stdio (Claude Desktop) and SSE (remote access)

## Available Tools

### Query Tools (Read-Only)

| Tool | Description |
|------|-------------|
| `inventory_get_inventory` | Get complete overview of all inventory |
| `inventory_get_containers` | List all available containers/locations |
| `inventory_get_container` | Show contents of a specific container |
| `inventory_find_location` | Find where a specific item is located |

### Management Tools (Mutating)

| Tool | Description |
|------|-------------|
| `inventory_create_items` | Add items to a container (supports dry_run) |
| `inventory_delete_items` | Remove items from a container (supports dry_run) |

### Meta Tool

| Tool | Description |
|------|-------------|
| `inventory` | Natural language command interface (routes to appropriate tool) |

## Installation

### 1. Install Dependencies

```bash
# From the mcp_server directory
cd /home/alan/workspace/OrganizerServer/organizerserver/mcp_server
source venv/bin/activate
pip install -r requirements.txt
```

The requirements include:
- `mcp>=1.0.0` - MCP protocol support
- `aiohttp>=3.9.0` - Async HTTP client
- `pydantic>=2.0.0` - Data validation

### 2. Configure Environment

Set the Java Inventory API URL:

```bash
export INVENTORY_API_BASE_URL="http://localhost:39805"
```

Alternative (fallback) environment variable:
```bash
export ORGANIZER_SERVER_URL="http://localhost:39805"
```

### 3. Configure Claude Desktop

Add the following to your Claude Desktop configuration:

**macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`

**Windows**: `%APPDATA%/Claude/claude_desktop_config.json`

**Linux**: `~/.config/Claude/claude_desktop_config.json`

```json
{
  "mcpServers": {
    "inventory": {
      "command": "bash",
      "args": [
        "-c",
        "cd /home/alan/workspace/OrganizerServer/organizerserver/mcp_server && source venv/bin/activate && export PYTHONPATH=/home/alan/workspace/OrganizerServer/organizerserver/mcp_server && export INVENTORY_API_BASE_URL=http://localhost:39805 && exec python3 -m mcp_inventory.server"
      ]
    }
  }
}
```

**Note**: Adjust the paths and URL according to your setup.

See `claude_desktop_config.json` in this directory for a complete example.

## Usage Examples

Once configured, you can interact with your inventory through natural language in Claude Desktop:

### Query Operations

- "Show me all my inventory"
- "What containers do I have?"
- "What's in the toolbox?"
- "Where are the nails?"

### Add Items

- "Add 3 hammers and a box of nails to the toolbox"
- "Put a laptop in the office container"
- "Create items: screwdriver, wrench, pliers in the garage"

### Remove Items

- "Remove the hammer from the toolbox"
- "Delete 2 boxes of nails from the garage"
- "Take out the old laptop from the office"

### Using Dry-Run Mode

Claude will automatically use dry-run mode when appropriate, but you can request it:

- "Show me what would happen if I added 5 hammers to the toolbox"
- "Preview removing the nails from the garage"

## Running the Server

### Using the Helper Script (Recommended)

```bash
cd /home/alan/workspace/OrganizerServer/organizerserver/mcp_server
./start_mcp_server.sh
```

### Stdio Transport (for Claude Desktop)

```bash
# From mcp_server directory with venv activated
cd /home/alan/workspace/OrganizerServer/organizerserver/mcp_server
source venv/bin/activate
export INVENTORY_API_BASE_URL="http://localhost:39805"
export PYTHONPATH=/home/alan/workspace/OrganizerServer/organizerserver/mcp_server
python3 -m mcp_inventory.server
```

### SSE Transport (for remote access)

```bash
cd /home/alan/workspace/OrganizerServer/organizerserver/mcp_server
source venv/bin/activate
export INVENTORY_API_BASE_URL="http://localhost:39805"
export PYTHONPATH=/home/alan/workspace/OrganizerServer/organizerserver/mcp_server
python3 -m mcp_inventory.server --transport sse --port 3000
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `INVENTORY_API_BASE_URL` | Java Inventory API base URL | None (required) |
| `ORGANIZER_SERVER_URL` | Alternative API URL (fallback) | None |
| `MCP_TRANSPORT` | Transport type (`stdio` or `sse`) | `stdio` |
| `MCP_PORT` | Port for SSE transport | `3000` |
| `MCP_HOST` | Host for SSE transport | `0.0.0.0` |
| `PYTHONPATH` | Python module search path | Must include mcp_server directory |

## Development

### Project Structure

```
organizerserver/mcp_server/
├── venv/                  # Python virtual environment
├── requirements.txt       # Python dependencies
├── start_mcp_server.sh   # Helper script to start server
├── run_tests.sh          # Helper script to run tests
├── tests/                # Test suite
│   ├── __init__.py
│   └── test_mcp_inventory.py
└── mcp_inventory/
    ├── __init__.py
    ├── server.py              # Main MCP server entry point
    ├── README.md              # This file
    ├── claude_desktop_config.json  # Example configuration
    ├── client/
    │   ├── __init__.py
    │   └── inventory_api.py   # Java API client wrapper
    ├── dependencies/
    │   ├── __init__.py
    │   └── integrations/      # Copied from helper project
    │       └── inventory_rest_client.py
    └── tools/
        ├── __init__.py
        ├── items.py          # create_items, delete_items
        └── queries.py        # get_inventory, get_container, etc.
```

### Testing

```bash
# Using the helper script
cd /home/alan/workspace/OrganizerServer/organizerserver/mcp_server
./run_tests.sh

# Or manually
cd /home/alan/workspace/OrganizerServer/organizerserver/mcp_server
source venv/bin/activate
export PYTHONPATH=/home/alan/workspace/OrganizerServer/organizerserver/mcp_server
python3 -m pytest tests/
```

### Testing with MCP Inspector

```bash
# Install MCP Inspector (one-time)
npm install -g @anthropics/mcp-inspector

# Run inspector
npx @anthropics/mcp-inspector

# Then connect to your server
```

## Migration from WebSocket

This MCP server replaces the WebSocket-based inventory system for Claude Desktop integration. The WebSocket server can still be used for:

- Android voice assistant app
- Other real-time voice interfaces
- Custom clients that require WebSocket

### Benefits of MCP over WebSocket

| Aspect | WebSocket | MCP Server |
|--------|-----------|------------|
| Architecture | Complex (session, state machine) | Simple (stateless) |
| Latency | ~3.5s (with LLM parsing) | ~0.3s (direct) |
| Cost | 4-8 LLM calls per operation | 0 LLM calls |
| Conversation | Server-managed | Claude-managed |
| Confirmation | Server-side state | Client-side (Claude) |
| Integration | Custom protocol | Standard MCP |

The MCP server provides:
- Simpler architecture (no session management)
- Lower latency (no LLM parsing overhead)
- Natural conversation flow (Claude handles parsing)
- No confirmation state machine needed
- Standard protocol (Model Context Protocol)

## Troubleshooting

### Server won't start

1. Check that environment variables are set:
   ```bash
   echo $INVENTORY_API_BASE_URL
   echo $ORGANIZER_SERVER_URL
   echo $PYTHONPATH
   ```

2. Verify the virtual environment is activated:
   ```bash
   which python3
   # Should point to mcp_server/venv/bin/python3
   ```

3. Check that dependencies are installed:
   ```bash
   pip list | grep mcp
   ```

### Tools not appearing in Claude Desktop

1. Check Claude Desktop logs:
   - macOS: `~/Library/Logs/Claude/mcp*.log`
   - Windows: `%APPDATA%/Claude/Logs/mcp*.log`
   - Linux: `~/.config/Claude/logs/mcp*.log`

2. Verify the server runs standalone:
   ```bash
   cd /home/alan/workspace/OrganizerServer/organizerserver/mcp_server
   ./start_mcp_server.sh
   ```

3. Check that the paths in `claude_desktop_config.json` are correct

4. Restart Claude Desktop after configuration changes

### API connection errors

Verify the Java Inventory API is running:

```bash
curl http://localhost:39805/inventory
```

If you get a connection error:
1. Start the Java Inventory API server
2. Verify the port matches (default: 39805)
3. Check firewall settings

### Import errors

If you get import errors like "ModuleNotFoundError: No module named 'mcp_inventory'":

1. Ensure PYTHONPATH is set correctly:
   ```bash
   export PYTHONPATH=/home/alan/workspace/OrganizerServer/organizerserver/mcp_server
   ```

2. Or run from the correct directory:
   ```bash
   cd /home/alan/workspace/OrganizerServer/organizerserver/mcp_server
   python3 -m mcp_inventory.server
   ```

## Performance Notes

- **Query operations**: ~100-300ms (direct API calls)
- **Mutating operations**: ~200-500ms (includes validation)
- **Dry-run operations**: ~150-400ms (validation without execution)
- **No LLM overhead**: Direct tool calls, no intermediate parsing

## Security Considerations

- The MCP server uses stdio transport for Claude Desktop (local only)
- SSE transport is intended for controlled environments
- SSE transport enforces `Authorization: Bearer <MCP_API_KEY>` when `MCP_API_KEY` is set
- Relies on Java API's security mechanisms for inventory operations
- Environment variables should not contain sensitive credentials unless required

## License

Same as the main OrganizerServer project.

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review the MCP protocol documentation
3. Check Claude Desktop logs
4. Verify Java API is running and accessible
