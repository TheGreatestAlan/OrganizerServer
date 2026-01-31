#!/usr/bin/env python3
"""MCP Inventory Server

A Model Context Protocol server that exposes inventory management operations
to Claude Desktop and other MCP clients.

This server provides tools for:
- Querying inventory (get_inventory, get_container, find_location, get_containers)
- Managing items (create_items, delete_items)

Environment Variables:
    INVENTORY_API_BASE_URL: Java Inventory API base URL
    ORGANIZER_SERVER_URL: Alternative env var for API URL (fallback)
    MCP_TRANSPORT: Transport type - 'stdio' (default) or 'sse'
    MCP_PORT: Port for SSE transport (default: 3000)
    MCP_HOST: Host for SSE transport (default: 0.0.0.0)

Usage:
    # Run with stdio transport (for Claude Desktop)
    python -m agent_server.mcp_inventory.server

    # Run with SSE transport (for remote access)
    python -m agent_server.mcp_inventory.server --transport sse --port 3000

    # Using the module directly
    python agent_server/mcp_inventory/server.py
"""

import argparse
import asyncio
import hmac
import logging
import os
import sys
from typing import Optional

# Configure logging before importing other modules
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    stream=sys.stderr
)
logger = logging.getLogger(__name__)

# Add parent directory to path for imports
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from mcp.server.fastmcp import FastMCP, Context
from mcp_inventory.tools.items import create_items, delete_items
from mcp_inventory.tools.queries import (
    get_inventory,
    get_container,
    find_location,
    get_containers,
)
from starlette.responses import PlainTextResponse

# Create MCP server instance
mcp = FastMCP("inventory")


# Register query tools (read-only)
@mcp.tool()
async def inventory_get_inventory() -> str:
    """Get a complete overview of all inventory across all containers.

    Use this tool when the user wants to see all their inventory,
    get a summary of what they have, or list everything in the system.

    Returns:
        A human-readable summary of all inventory including containers and item counts.
    """
    return await get_inventory()


@mcp.tool()
async def inventory_get_container(container_id: str) -> str:
    """Get the contents of a specific container.

    Use this tool when the user wants to see what's inside a specific container,
    check the contents of a location, or list items in a box, room, or storage area.

    Args:
        container_id: The container ID or name (e.g., "toolbox", "garage", "kitchen")

    Returns:
        A human-readable list of items in the specified container.
    """
    return await get_container(container_id)


@mcp.tool()
async def inventory_find_location(item_name: str) -> str:
    """Find where a specific item is located in the inventory.

    Use this tool when the user wants to find where something is stored,
    locate a specific item, or know which container has an item.

    Args:
        item_name: The name of the item to find (e.g., "hammer", "nails", "laptop")

    Returns:
        A human-readable description of where the item is located.
    """
    return await find_location(item_name)


@mcp.tool()
async def inventory_get_containers() -> str:
    """Get a list of all available containers/locations in the inventory.

    Use this tool when the user wants to see what containers exist,
    know available storage locations, or get an overview of their storage organization.

    Returns:
        A human-readable list of all containers with item counts.
    """
    return await get_containers()


# Register item management tools (mutating)
@mcp.tool()
async def inventory_create_items(
    container: str,
    items: list,
    dry_run: bool = False
) -> str:
    """Add items to a container in the inventory.

    Use this tool when the user wants to add, create, or put items into a container.

    Examples:
        - "Add 3 hammers to the toolbox"
        - "Put a box of nails in the garage"
        - "Create items: laptop, charger, mouse in the office"

    Args:
        container: The container ID or name (e.g., "toolbox", "garage", "kitchen")
        items: List of item descriptions to add (e.g., ["3 hammers", "box of nails"])
        dry_run: If True, preview what would be added without executing.
                Use this to show the user what will happen before making changes.

    Returns:
        A human-readable description of what was added and the updated container contents.
    """
    return await create_items(container, items, dry_run)


@mcp.tool()
async def inventory_delete_items(
    container: str,
    items: list,
    dry_run: bool = False
) -> str:
    """Remove items from a container in the inventory.

    Use this tool when the user wants to remove, delete, or take items out of a container.

    Examples:
        - "Remove the hammer from the toolbox"
        - "Delete 2 boxes of nails from the garage"
        - "Take out the old laptop from the office"

    Args:
        container: The container ID or name (e.g., "toolbox", "garage")
        items: List of item descriptions to remove (e.g., ["hammer", "box of nails"])
        dry_run: If True, preview what would be removed without executing.
                Use this to show the user what will happen before making changes.

    Returns:
        A human-readable description of what was removed and the updated container contents.
    """
    return await delete_items(container, items, dry_run)


# Register a natural language meta-tool (optional - for simpler UX)
@mcp.tool()
async def inventory(
    command: str,
    dry_run: bool = False
) -> str:
    """Execute a natural language inventory command.

    This is a flexible tool that accepts natural language commands for inventory management.
    Use this when the user speaks in natural language rather than explicit tool calls.

    Examples of valid commands:
        - "Add 3 hammers and a box of nails to the toolbox"
        - "Where are the nails?"
        - "Show me the garage contents"
        - "Remove the hammer from the toolbox"
        - "What do I have in inventory?"
        - "List all containers"

    Args:
        command: Natural language inventory command
        dry_run: If True, preview what would happen without executing.
                Recommended for destructive operations or when uncertain.

    Returns:
        Result of the inventory operation in human-readable format.

    Note:
        This tool parses the command and delegates to the appropriate discrete tool.
        For precise control, use the specific tools (inventory_create_items,
        inventory_delete_items, inventory_get_container, etc.) directly.
    """
    # This is a meta-tool that provides natural language interface
    # The actual parsing and delegation is handled by Claude based on tool descriptions
    # We return guidance on which tools to use

    command_lower = command.lower().strip()

    # Simple keyword-based routing for direct calls
    if any(word in command_lower for word in ["add", "create", "put", "place", "insert"]):
        return (
            "This command appears to be for adding items. "
            "Please use the 'inventory_create_items' tool with:\n"
            "- container: the target container name\n"
            "- items: list of items to add\n"
            f"- dry_run: {dry_run}"
        )
    elif any(word in command_lower for word in ["remove", "delete", "take out", "discard"]):
        return (
            "This command appears to be for removing items. "
            "Please use the 'inventory_delete_items' tool with:\n"
            "- container: the source container name\n"
            "- items: list of items to remove\n"
            f"- dry_run: {dry_run}"
        )
    elif any(word in command_lower for word in ["where", "find", "locate", "search"]):
        return (
            "This command appears to be for finding an item. "
            "Please use the 'inventory_find_location' tool with:\n"
            "- item_name: the name of the item to find"
        )
    elif any(word in command_lower for word in ["show", "get", "list", "what's in", "contents of"]):
        if "container" in command_lower or "all" in command_lower:
            return (
                "This command appears to be for listing containers. "
                "Please use either:\n"
                "- 'inventory_get_containers' to list all containers\n"
                "- 'inventory_get_container' with container_id to see specific container contents"
            )
        else:
            return (
                "This command appears to be for viewing inventory. "
                "Please use either:\n"
                "- 'inventory_get_inventory' for complete overview\n"
                "- 'inventory_get_container' with container_id for specific container"
            )
    else:
        return (
            "I can help with inventory management. Please use one of these specific tools:\n"
            "- inventory_get_inventory: See all inventory\n"
            "- inventory_get_containers: List all containers\n"
            "- inventory_get_container: See specific container contents\n"
            "- inventory_find_location: Find where an item is\n"
            "- inventory_create_items: Add items to a container\n"
            "- inventory_delete_items: Remove items from a container"
        )


def get_inventory_api_url() -> Optional[str]:
    """Get the inventory API URL from environment variables."""
    return os.getenv("INVENTORY_API_BASE_URL") or os.getenv("ORGANIZER_SERVER_URL")


def get_mcp_api_key() -> Optional[str]:
    """Get MCP API key for SSE auth."""
    return os.getenv("MCP_API_KEY")


def extract_bearer_token(auth_header: str | None) -> Optional[str]:
    """Extract bearer token from an Authorization header."""
    if not auth_header:
        return None
    parts = auth_header.split()
    if len(parts) != 2:
        return None
    if parts[0].lower() != "bearer":
        return None
    return parts[1]


def is_authorized(auth_header: str | None, api_key: str) -> bool:
    """Validate Authorization header against the configured API key."""
    token = extract_bearer_token(auth_header)
    if not token:
        return False
    return hmac.compare_digest(token, api_key)


class ApiKeyAuthMiddleware:
    """Simple ASGI middleware enforcing Bearer API key auth."""

    def __init__(self, app, api_key: str):
        self.app = app
        self.api_key = api_key

    async def __call__(self, scope, receive, send):
        if scope.get("type") == "http":
            headers = {k.decode().lower(): v.decode() for k, v in scope.get("headers", [])}
            if not is_authorized(headers.get("authorization"), self.api_key):
                response = PlainTextResponse("Unauthorized", status_code=401)
                response.headers["WWW-Authenticate"] = "Bearer"
                await response(scope, receive, send)
                return
        await self.app(scope, receive, send)


def validate_environment():
    """Validate that required environment variables are set."""
    api_url = get_inventory_api_url()
    if not api_url:
        logger.error("No inventory API URL configured!")
        logger.error("Please set one of these environment variables:")
        logger.error("  - INVENTORY_API_BASE_URL")
        logger.error("  - ORGANIZER_SERVER_URL")
        sys.exit(1)
    logger.info(f"Using inventory API at: {api_url}")


def main():
    """Main entry point for the MCP server."""
    parser = argparse.ArgumentParser(
        description="MCP Inventory Server",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Environment Variables:
  INVENTORY_API_BASE_URL  Java Inventory API base URL
  ORGANIZER_SERVER_URL    Alternative API URL (fallback)
  MCP_TRANSPORT           Transport type: 'stdio' (default) or 'sse'
  MCP_PORT                Port for SSE transport (default: 3000)
  MCP_HOST                Host for SSE transport (default: 0.0.0.0)

Examples:
  # Run with stdio transport (for Claude Desktop)
  python -m agent_server.mcp_inventory.server

  # Run with SSE transport
  python -m agent_server.mcp_inventory.server --transport sse --port 3000
        """
    )
    parser.add_argument(
        "--transport",
        choices=["stdio", "sse"],
        default=os.getenv("MCP_TRANSPORT", "stdio"),
        help="Transport type (default: stdio)"
    )
    parser.add_argument(
        "--port",
        type=int,
        default=int(os.getenv("MCP_PORT", "3000")),
        help="Port for SSE transport (default: 3000)"
    )
    parser.add_argument(
        "--host",
        default=os.getenv("MCP_HOST", "0.0.0.0"),
        help="Host for SSE transport (default: 0.0.0.0)"
    )

    args = parser.parse_args()

    # Validate environment
    validate_environment()
    api_key = get_mcp_api_key() if args.transport == "sse" else None
    if args.transport == "sse" and not api_key:
        logger.error("MCP_API_KEY must be set for SSE transport.")
        sys.exit(1)

    # Log startup info
    logger.info(f"Starting MCP Inventory Server")
    logger.info(f"Transport: {args.transport}")

    if args.transport == "sse":
        logger.info(f"Host: {args.host}")
        logger.info(f"Port: {args.port}")

    try:
        if args.transport == "stdio":
            # Run with stdio transport (default for Claude Desktop)
            mcp.run(transport="stdio")
        else:
            # Run with SSE transport + API key auth
            import uvicorn

            sse_app = mcp.sse_app()
            authed_app = ApiKeyAuthMiddleware(sse_app, api_key)
            config = uvicorn.Config(
                authed_app,
                host=args.host,
                port=args.port,
                log_level="info",
            )
            server = uvicorn.Server(config)
            server.run()
    except KeyboardInterrupt:
        logger.info("Shutting down...")
    except Exception as e:
        logger.error(f"Error running server: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
