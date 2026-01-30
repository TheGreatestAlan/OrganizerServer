#!/bin/bash
# Run MCP server test suite

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR"

# Activate virtual environment
source venv/bin/activate

# Set test environment
export INVENTORY_API_BASE_URL=http://localhost:39805
export PYTHONPATH="$SCRIPT_DIR"

echo "Running MCP Server Test Suite"
echo "=============================="
echo ""

# Run component tests
echo "1. Component Tests:"
python3 tests/test_mcp_inventory.py
echo ""

# Only run live tests if Java API is available
if curl -s http://localhost:39805/inventory > /dev/null 2>&1; then
    echo "2. Live API Tests:"
    python3 tests/test_mcp_live.py
    echo ""

    echo "3. Mutation Tests:"
    python3 tests/test_mcp_mutations.py
else
    echo "⚠️  Skipping live tests - Java API not running at localhost:39805"
fi

echo ""
echo "Test suite complete!"
