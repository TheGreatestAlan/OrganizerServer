#!/bin/bash
# Start MCP Inventory Server for Claude Desktop
#
# Usage:
#   ./start_mcp_server.sh              # Use default port 39805
#   ./start_mcp_server.sh 8080         # Use custom port

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR"

# Get port from argument or use default
PORT=${1:-39805}

# Activate virtual environment
source venv/bin/activate

# Set environment variables
export PYTHONPATH="$SCRIPT_DIR"
export INVENTORY_API_BASE_URL=http://localhost:$PORT

# Run the MCP server with stdio transport (for Claude Desktop)
echo "Starting MCP Inventory Server..."
echo "Java API: $INVENTORY_API_BASE_URL"
echo "Transport: stdio (Claude Desktop)"
echo ""

python3 -m mcp_inventory.server
