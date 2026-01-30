#!/usr/bin/env python3
"""Test script for MCP Inventory Server.

This script tests the MCP server components without requiring
the actual Java Inventory API to be running.
"""

import asyncio
import sys
import os

# Add mcp_server to path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from mcp_inventory.server import mcp


async def test_list_tools():
    """Test that all tools are registered."""
    print("Testing tool registration...")
    tools = await mcp.list_tools()

    expected_tools = {
        'inventory_get_inventory',
        'inventory_get_container',
        'inventory_find_location',
        'inventory_get_containers',
        'inventory_create_items',
        'inventory_delete_items',
        'inventory'
    }

    actual_tools = {tool.name for tool in tools}

    print(f"  Registered tools: {actual_tools}")

    missing = expected_tools - actual_tools
    if missing:
        print(f"  ❌ Missing tools: {missing}")
        return False

    extra = actual_tools - expected_tools
    if extra:
        print(f"  ⚠️  Extra tools: {extra}")

    print(f"  ✅ All {len(expected_tools)} expected tools registered")
    return True


async def test_tool_schemas():
    """Test that tool schemas are valid."""
    print("\nTesting tool schemas...")
    tools = await mcp.list_tools()

    for tool in tools:
        if not tool.description:
            print(f"  ❌ Tool '{tool.name}' missing description")
            return False
        print(f"  ✅ {tool.name}: {tool.description[:50]}...")

    return True


async def test_client_imports():
    """Test that client module imports work."""
    print("\nTesting client module imports...")

    try:
        from mcp_inventory.client.inventory_api import (
            MCPInventoryClient,
            get_inventory_client,
            InventoryAPIError
        )
        print("  ✅ MCPInventoryClient imported")
        print("  ✅ get_inventory_client imported")
        print("  ✅ InventoryAPIError imported")
        return True
    except Exception as e:
        print(f"  ❌ Import error: {e}")
        return False


async def test_tools_imports():
    """Test that tools modules import work."""
    print("\nTesting tools module imports...")

    try:
        from mcp_inventory.tools.items import create_items, delete_items
        from mcp_inventory.tools.queries import (
            get_inventory,
            get_container,
            find_location,
            get_containers
        )
        print("  ✅ create_items imported")
        print("  ✅ delete_items imported")
        print("  ✅ get_inventory imported")
        print("  ✅ get_container imported")
        print("  ✅ find_location imported")
        print("  ✅ get_containers imported")
        return True
    except Exception as e:
        print(f"  ❌ Import error: {e}")
        return False


async def main():
    """Run all tests."""
    print("=" * 60)
    print("MCP Inventory Server Test Suite")
    print("=" * 60)

    results = []

    results.append(await test_list_tools())
    results.append(await test_tool_schemas())
    results.append(await test_client_imports())
    results.append(await test_tools_imports())

    print("\n" + "=" * 60)
    if all(results):
        print("✅ All tests passed!")
        return 0
    else:
        print("❌ Some tests failed!")
        return 1


if __name__ == "__main__":
    exit_code = asyncio.run(main())
    sys.exit(exit_code)
