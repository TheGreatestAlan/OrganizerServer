#!/usr/bin/env python3
"""Test MCP mutation operations (create/delete) with live API."""

import asyncio
import os
import sys

# Add mcp_server to path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

# Set the inventory API URL
os.environ['INVENTORY_API_BASE_URL'] = 'http://localhost:39805'

from mcp_inventory.tools.queries import get_container
from mcp_inventory.tools.items import create_items, delete_items


async def test_full_mutation_cycle():
    """Test create and delete with a real item."""
    print("=" * 70)
    print("MCP Mutation Test - Full Create/Delete Cycle")
    print("=" * 70)
    print()

    container = "4"
    test_item = "MCP_TEST_ITEM_DELETE_ME"

    # Step 1: Check initial state
    print(f"Step 1: Initial state of container {container}")
    print("-" * 70)
    initial = await get_container(container)
    print(initial)
    print()

    # Step 2: Add test item
    print(f"Step 2: Adding '{test_item}' to container {container}")
    print("-" * 70)
    create_result = await create_items(container, [test_item])
    print(create_result)
    print()

    # Step 3: Verify item was added
    print(f"Step 3: Verify item was added")
    print("-" * 70)
    after_create = await get_container(container)
    print(after_create)
    print()

    # Step 4: Delete test item
    print(f"Step 4: Deleting '{test_item}' from container {container}")
    print("-" * 70)
    delete_result = await delete_items(container, [test_item])
    print(delete_result)
    print()

    # Step 5: Verify item was deleted
    print(f"Step 5: Verify item was deleted")
    print("-" * 70)
    after_delete = await get_container(container)
    print(after_delete)
    print()

    # Step 6: Test dry-run mode
    print(f"Step 6: Test dry-run mode (should not actually delete)")
    print("-" * 70)
    dry_run_result = await delete_items(container, ["Thyme"], dry_run=True)
    print(dry_run_result)
    print()

    # Step 7: Verify nothing was deleted
    print(f"Step 7: Verify dry-run didn't actually delete")
    print("-" * 70)
    final_check = await get_container(container)
    print(final_check)
    print()

    print("=" * 70)
    print("✅ All mutation tests completed successfully!")
    print("=" * 70)


async def test_error_cases():
    """Test error handling."""
    print()
    print("=" * 70)
    print("MCP Error Handling Tests")
    print("=" * 70)
    print()

    # Test 1: Delete non-existent item
    print("Test 1: Delete non-existent item")
    print("-" * 70)
    result = await delete_items("4", ["ITEM_THAT_DOES_NOT_EXIST"])
    print(result)
    print()

    # Test 2: Add to non-existent container
    print("Test 2: Add to non-existent container")
    print("-" * 70)
    result = await create_items("999999", ["test"])
    print(result)
    print()


async def main():
    """Run all tests."""
    await test_full_mutation_cycle()
    await test_error_cases()


if __name__ == "__main__":
    asyncio.run(main())
