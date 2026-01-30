"""MCP tools for inventory item operations (create, delete).

These tools handle mutating operations on inventory items.
"""

from typing import List, Optional

from mcp.server.fastmcp import Context

from agent_server.mcp_inventory.client.inventory_api import (
    get_inventory_client,
    InventoryAPIError,
)


async def create_items(
    container: str,
    items: List[str],
    dry_run: bool = False,
    ctx: Optional[Context] = None,
) -> str:
    """Add items to a container in the inventory.

    Use this tool when the user wants to add, create, or put items into a container.
    The items parameter accepts a list of natural language item descriptions.

    Examples of valid item descriptions:
    - "3 hammers"
    - "box of nails"
    - "screwdriver set"
    - "laptop computer"

    Args:
        container: The container ID or name (e.g., "toolbox", "garage", "kitchen")
        items: List of item descriptions to add
        dry_run: If True, preview what would be added without executing the operation.
                Use this to show the user what will happen before making changes.

    Returns:
        A human-readable description of what was added and the updated container contents.
    """
    try:
        client = get_inventory_client()
        result = client.create_items(container, items, dry_run=dry_run)

        if dry_run:
            items_text = ", ".join(items)
            current = ", ".join(result.get("current_contents", [])) or "empty"
            return (
                f"[DRY RUN] Would add the following items to '{container}':\n"
                f"  Items: {items_text}\n"
                f"  Current contents: {current}\n"
                f"\nCall with dry_run=False to execute."
            )

        # Handle boolean response from Java API
        if isinstance(result, bool):
            if result:
                # Get updated container contents to show what was added
                try:
                    updated = client.get_container(container)
                    contents = updated.get("contents", [])
                    contents_text = ", ".join(contents) if contents else "empty"
                except:
                    contents_text = "(unknown)"

                items_text = ", ".join(items)
                return (
                    f"✅ Successfully added {len(items)} item(s) to '{container}':\n"
                    f"  Added: {items_text}\n"
                    f"\n'{container}' now contains: {contents_text}"
                )
            else:
                return f"❌ Failed to add items to '{container}'. Container may not exist."

        # Handle structured response (if API format changes in future)
        items_added = result.get("items_modified", result.get("items_added", items))
        items_text = ", ".join(items_added) if isinstance(items_added, list) else str(items_added)

        final_contents = result.get("final_container_contents", [])
        contents_text = ", ".join(final_contents) if final_contents else "empty"

        action = result.get("action", "added")
        container_id = result.get("container_id", container)

        return (
            f"Successfully {action} {len(items_added)} item(s) to '{container_id}':\n"
            f"  Added: {items_text}\n"
            f"\n'{container_id}' now contains: {contents_text}"
        )

    except InventoryAPIError as e:
        return f"Error adding items: {e.message}"
    except Exception as e:
        return f"Unexpected error: {str(e)}"


async def delete_items(
    container: str,
    items: List[str],
    dry_run: bool = False,
    ctx: Optional[Context] = None,
) -> str:
    """Remove items from a container in the inventory.

    Use this tool when the user wants to remove, delete, or take items out of a container.
    The items parameter accepts a list of natural language item descriptions.

    Examples of valid item descriptions:
    - "hammer"
    - "3 boxes of nails"
    - "screwdriver"

    Args:
        container: The container ID or name (e.g., "toolbox", "garage")
        items: List of item descriptions to remove
        dry_run: If True, preview what would be removed without executing the operation.
                Use this to show the user what will happen before making changes.

    Returns:
        A human-readable description of what was removed and the updated container contents.
    """
    try:
        client = get_inventory_client()
        result = client.delete_items(container, items, dry_run=dry_run)

        if dry_run:
            items_text = ", ".join(items)
            current = ", ".join(result.get("current_contents", [])) or "empty"
            return (
                f"[DRY RUN] Would remove the following items from '{container}':\n"
                f"  Items: {items_text}\n"
                f"  Current contents: {current}\n"
                f"\nCall with dry_run=False to execute."
            )

        # Handle boolean response from Java API
        if isinstance(result, bool):
            if result:
                # Get updated container contents to show what's left
                try:
                    updated = client.get_container(container)
                    contents = updated.get("contents", [])
                    contents_text = ", ".join(contents) if contents else "empty"
                except:
                    contents_text = "(unknown)"

                items_text = ", ".join(items)
                return (
                    f"✅ Successfully removed {len(items)} item(s) from '{container}':\n"
                    f"  Removed: {items_text}\n"
                    f"\n'{container}' now contains: {contents_text}"
                )
            else:
                return f"❌ Failed to remove items from '{container}'. Items may not exist or container not found."

        # Handle structured response (if API format changes in future)
        items_removed = result.get("items_modified", result.get("items_removed", items))
        items_text = ", ".join(items_removed) if isinstance(items_removed, list) else str(items_removed)

        final_contents = result.get("final_container_contents", [])
        contents_text = ", ".join(final_contents) if final_contents else "empty"

        action = result.get("action", "removed")
        container_id = result.get("container_id", container)

        return (
            f"Successfully {action} {len(items_removed)} item(s) from '{container_id}':\n"
            f"  Removed: {items_text}\n"
            f"\n'{container_id}' now contains: {contents_text}"
        )

    except InventoryAPIError as e:
        return f"Error removing items: {e.message}"
    except Exception as e:
        return f"Unexpected error: {str(e)}"
