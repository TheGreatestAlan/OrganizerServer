"""MCP tools for container operations (create, rename).

These tools handle mutating operations on containers (locations).
"""

from typing import Optional

from mcp.server.fastmcp import Context

from mcp_inventory.client.inventory_api import (
    get_inventory_client,
    InventoryAPIError,
)


async def create_container(
    name: str,
    description: Optional[str] = None,
    dry_run: bool = False,
    ctx: Optional[Context] = None,
) -> str:
    """Create a new empty container in the inventory.

    Use this tool when the user wants to:
    - Add a new container, box, room, or storage location
    - Create a location before putting items into it

    Examples:
        - "Add a container called garage-shelf"
        - "Create a new location for tools"

    Args:
        name: The name/ID for the new container (e.g., "garage-shelf-2")
        description: Optional location description (e.g., "Second shelf in the garage")
        dry_run: If True, preview what would be created without executing.

    Returns:
        A human-readable description of the created container.
    """
    try:
        client = get_inventory_client()
        result = client.create_container(name, description=description, dry_run=dry_run)

        if dry_run:
            if result.get("conflict"):
                return (
                    f"[DRY RUN] Container '{name}' already exists.\n"
                    f"Choose a different name, or call with dry_run=False to confirm."
                )
            desc_text = f"\n  Description: {description}" if description else ""
            return (
                f"[DRY RUN] Would create container '{name}':{desc_text}\n"
                f"\nCall with dry_run=False to execute."
            )

        desc_text = f" ({description})" if description else ""
        return f"✅ Successfully created container '{name}'{desc_text}."

    except InventoryAPIError as e:
        if "409" in e.message:
            return f"❌ Container '{name}' already exists."
        return f"Error creating container: {e.message}"
    except Exception as e:
        return f"Unexpected error: {str(e)}"


async def rename_container(
    container_id: str,
    new_name: Optional[str] = None,
    description: Optional[str] = None,
    dry_run: bool = False,
    ctx: Optional[Context] = None,
) -> str:
    """Rename a container and/or update its location description.

    Use this tool when the user wants to:
    - Rename a container, box, room, or storage location
    - Update where a container is described as being located
    - All items in the container carry over to the new name

    Examples:
        - "Rename the toolbox to red-toolbox"
        - "Update the garage container's description"

    Args:
        container_id: The current container ID or name (e.g., "toolbox")
        new_name: The new container ID or name (omit for description-only update)
        description: New location description (omit to keep the current one)
        dry_run: If True, preview what would change without executing.

    Returns:
        A human-readable description of what changed.
    """
    if not new_name and not description:
        return "❌ Please provide a new name and/or a new description."

    try:
        client = get_inventory_client()
        result = client.rename_container(
            container_id, new_name=new_name, description=description, dry_run=dry_run
        )

        if dry_run:
            if result.get("not_found"):
                return f"[DRY RUN] Container '{container_id}' was not found."
            item_count = result.get("item_count", 0)
            target = new_name or container_id
            return (
                f"[DRY RUN] Would update container '{container_id}':\n"
                f"  New name: {target}\n"
                f"  Items that would move: {item_count}\n"
                f"\nCall with dry_run=False to execute."
            )

        changes = []
        if new_name:
            changes.append(f"renamed to '{new_name}'")
        if description:
            changes.append(f"description updated to '{description}'")
        return f"✅ Container '{container_id}' {' and '.join(changes)}."

    except InventoryAPIError as e:
        if "409" in e.message:
            return f"❌ A container named '{new_name}' already exists."
        if "404" in e.message:
            return f"❌ Container '{container_id}' not found."
        return f"Error updating container: {e.message}"
    except Exception as e:
        return f"Unexpected error: {str(e)}"
