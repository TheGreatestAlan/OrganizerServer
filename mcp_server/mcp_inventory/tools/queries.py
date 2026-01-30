"""MCP tools for inventory query operations (read-only).

These tools handle non-mutating queries on inventory data.
"""

from typing import Optional

from mcp.server.fastmcp import Context

from agent_server.mcp_inventory.client.inventory_api import (
    get_inventory_client,
    InventoryAPIError,
)


async def get_inventory(ctx: Optional[Context] = None) -> str:
    """Get a complete overview of all inventory across all containers.

    Use this tool when the user wants to:
    - See all their inventory
    - Get a summary of what they have
    - List everything in the system

    Returns:
        A human-readable summary of all inventory including containers and item counts.
    """
    try:
        client = get_inventory_client()
        result = client.get_inventory()

        containers = result.get("containers", [])
        total_items = result.get("total_items", 0)

        if not containers:
            return "No containers found in inventory."

        # Build summary
        lines = [f"Inventory Overview ({total_items} total items):", ""]

        for container in containers:
            name = container.get("name") or container.get("container_name") or container.get("id")
            contents = container.get("contents", [])
            item_count = len(contents)

            lines.append(f"📦 {name} ({item_count} items):")

            if contents:
                for item in contents:
                    lines.append(f"   • {item}")
            else:
                lines.append("   (empty)")
            lines.append("")

        return "\n".join(lines)

    except InventoryAPIError as e:
        return f"Error retrieving inventory: {e.message}"
    except Exception as e:
        return f"Unexpected error: {str(e)}"


async def get_container(container_id: str, ctx: Optional[Context] = None) -> str:
    """Get the contents of a specific container.

    Use this tool when the user wants to:
    - See what's inside a specific container
    - Check the contents of a location
    - List items in a box, room, or storage area

    Args:
        container_id: The container ID or name (e.g., "toolbox", "garage", "kitchen")

    Returns:
        A human-readable list of items in the specified container.
    """
    try:
        client = get_inventory_client()
        result = client.get_container(container_id)

        # Handle different response formats
        container_name = result.get("container_name") or result.get("container_id") or container_id
        contents = result.get("contents", [])

        if not contents:
            return f"📦 '{container_name}' is empty."

        lines = [f"📦 Contents of '{container_name}' ({len(contents)} items):", ""]

        for item in contents:
            lines.append(f"   • {item}")

        return "\n".join(lines)

    except InventoryAPIError as e:
        return f"Error retrieving container '{container_id}': {e.message}"
    except Exception as e:
        return f"Unexpected error: {str(e)}"


async def find_location(item_name: str, ctx: Optional[Context] = None) -> str:
    """Find where a specific item is located in the inventory.

    Use this tool when the user wants to:
    - Find where something is stored
    - Locate a specific item
    - Know which container has an item

    Args:
        item_name: The name of the item to find (e.g., "hammer", "nails", "laptop")

    Returns:
        A human-readable description of where the item is located.
    """
    try:
        client = get_inventory_client()
        result = client.find_location(item_name)

        # Handle different response formats
        found = result.get("found", False)
        locations = result.get("locations", [])

        if not found or not locations:
            return f"❌ '{item_name}' was not found in any container."

        # Build response
        if len(locations) == 1:
            loc = locations[0]
            container_name = loc.get("container_name") or loc.get("container_id")
            quantity = loc.get("quantity", 1)
            qty_text = f" ({quantity})" if quantity > 1 else ""
            return f"📍 '{item_name}'{qty_text} is in: {container_name}"
        else:
            lines = [f"📍 '{item_name}' found in {len(locations)} locations:", ""]
            for loc in locations:
                container_name = loc.get("container_name") or loc.get("container_id")
                quantity = loc.get("quantity", 1)
                qty_text = f" ({quantity})" if quantity > 1 else ""
                lines.append(f"   • {container_name}{qty_text}")
            return "\n".join(lines)

    except InventoryAPIError as e:
        return f"Error finding '{item_name}': {e.message}"
    except Exception as e:
        return f"Unexpected error: {str(e)}"


async def get_containers(ctx: Optional[Context] = None) -> str:
    """Get a list of all available containers/locations in the inventory.

    Use this tool when the user wants to:
    - See what containers exist
    - Know available storage locations
    - Get an overview of their storage organization

    Returns:
        A human-readable list of all containers with item counts.
    """
    try:
        client = get_inventory_client()
        containers = client.get_containers()

        if not containers:
            return "No containers found in inventory."

        lines = [f"Available Containers ({len(containers)} total):", ""]

        for container in containers:
            name = container.get("name") or container.get("id", "Unknown")
            item_count = container.get("item_count", 0)
            lines.append(f"📦 {name} ({item_count} items)")

        return "\n".join(lines)

    except InventoryAPIError as e:
        return f"Error retrieving containers: {e.message}"
    except Exception as e:
        return f"Unexpected error: {str(e)}"
