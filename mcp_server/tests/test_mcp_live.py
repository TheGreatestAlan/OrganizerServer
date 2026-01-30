#!/usr/bin/env python3
"""Test script for MCP Inventory Server with live Java API.

This script tests the MCP tools against the actual Java inventory API
running at http://localhost:39805.
"""

import asyncio
import os
import sys

# Add mcp_server to path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

# Set the inventory API URL
os.environ['INVENTORY_API_BASE_URL'] = 'http://localhost:39805'

from mcp_inventory.tools.queries import (
    get_inventory,
    get_container,
    find_location,
    get_containers
)
from mcp_inventory.tools.items import create_items, delete_items


async def test_get_inventory():
    """Test getting all inventory."""
    print("=" * 70)
    print("TEST: Get All Inventory")
    print("=" * 70)
    result = await get_inventory()
    print(result)
    print()
    return True


async def test_get_containers():
    """Test getting all containers."""
    print("=" * 70)
    print("TEST: Get All Containers")
    print("=" * 70)
    result = await get_containers()
    print(result)
    print()
    return True


async def test_get_container():
    """Test getting a specific container."""
    print("=" * 70)
    print("TEST: Get Container '1'")
    print("=" * 70)
    result = await get_container("1")
    print(result)
    print()
    return True


async def test_find_location():
    """Test finding item location."""
    print("=" * 70)
    print("TEST: Find Location of 'sesame'")
    print("=" * 70)
    result = await find_location("sesame")
    print(result)
    print()
    return True


async def test_create_items_dry_run():
    """Test creating items in dry-run mode."""
    print("=" * 70)
    print("TEST: Create Items (Dry Run)")
    print("=" * 70)
    result = await create_items("1", ["test hammer", "test wrench"], dry_run=True)
    print(result)
    print()
    return True


async def test_delete_items_dry_run():
    """Test deleting items in dry-run mode."""
    print("=" * 70)
    print("TEST: Delete Items (Dry Run)")
    print("=" * 70)
    result = await delete_items("1", ["Bay Leaves"], dry_run=True)
    print(result)
    print()
    return True


async def main():
    """Run all tests."""
    print("\n" + "=" * 70)
    print("MCP Inventory Server - Live API Testing")
    print("Testing against: http://localhost:39805")
    print("=" * 70 + "\n")

    tests = [
        ("Get Inventory", test_get_inventory),
        ("Get Containers", test_get_containers),
        ("Get Container", test_get_container),
        ("Find Location", test_find_location),
        ("Create Items (Dry Run)", test_create_items_dry_run),
        ("Delete Items (Dry Run)", test_delete_items_dry_run),
    ]

    results = []
    for name, test_func in tests:
        try:
            result = await test_func()
            results.append((name, result, None))
        except Exception as e:
            results.append((name, False, str(e)))
            print(f"❌ ERROR: {e}\n")

    # Print summary
    print("=" * 70)
    print("TEST SUMMARY")
    print("=" * 70)
    passed = 0
    failed = 0
    for name, result, error in results:
        if result:
            print(f"✅ {name}")
            passed += 1
        else:
            print(f"❌ {name}: {error}")
            failed += 1

    print(f"\nTotal: {passed + failed} tests")
    print(f"Passed: {passed}")
    print(f"Failed: {failed}")

    return 0 if failed == 0 else 1


if __name__ == "__main__":
    exit_code = asyncio.run(main())
    sys.exit(exit_code)
