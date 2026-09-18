"""Java Inventory API client wrapper for MCP server.

This module provides a simplified interface to the Java Inventory API
for use by MCP tools. It wraps the existing InventoryRestClient.
"""

import json
import os
from typing import Any, Dict, List, Optional

from mcp_inventory.dependencies.InventoryRestClient import InventoryClient
from mcp_inventory.dependencies.FunctionResponse import Status


class InventoryAPIError(Exception):
    """Exception raised for inventory API errors."""

    def __init__(self, message: str, status_code: Optional[int] = None):
        self.message = message
        self.status_code = status_code
        super().__init__(self.message)


class MCPInventoryClient:
    """MCP-friendly wrapper for the Inventory REST client.

    Provides a cleaner interface for MCP tools with proper error handling
    and response formatting.
    """

    def __init__(self, base_url: Optional[str] = None):
        """Initialize the client.

        Args:
            base_url: Java Inventory API base URL. If not provided,
                     reads from INVENTORY_API_BASE_URL or ORGANIZER_SERVER_URL env var.
        """
        self.base_url = base_url or self._get_base_url()
        self._client = InventoryClient(self.base_url)

    def _get_base_url(self) -> str:
        """Get base URL from environment variables."""
        # Try new env var first, then fall back to existing one
        base_url = os.getenv("INVENTORY_API_BASE_URL") or os.getenv("ORGANIZER_SERVER_URL")
        if not base_url:
            raise InventoryAPIError(
                "No inventory API base URL configured. "
                "Set INVENTORY_API_BASE_URL or ORGANIZER_SERVER_URL environment variable."
            )
        return base_url.rstrip("/")

    def _handle_response(self, response) -> Dict[str, Any]:
        """Handle FunctionResponse and extract data or raise error.

        Args:
            response: FunctionResponse from InventoryClient

        Returns:
            Parsed JSON data from response

        Raises:
            InventoryAPIError: If response indicates failure
        """
        if response.status == Status.FAILURE:
            raise InventoryAPIError(response.response)

        try:
            if response.response and response.response.strip():
                return json.loads(response.response)
            return {}
        except json.JSONDecodeError as e:
            # Return raw response if not JSON
            return {"message": response.response}

    def get_inventory(self) -> Dict[str, Any]:
        """Get all inventory data.

        Returns:
            Dictionary containing inventory data with containers and items.
        """
        response = self._client.get_inventory()
        raw_data = self._handle_response(response)

        # Transform flat dictionary format to structured format
        # Java API returns: {"0": [...], "1": [...]}
        # We need: {"containers": [{"id": "0", "contents": [...]}, ...]}
        if isinstance(raw_data, dict) and "containers" not in raw_data:
            containers = []
            for container_id, contents in raw_data.items():
                containers.append({
                    "id": container_id,
                    "name": container_id,
                    "contents": contents if isinstance(contents, list) else []
                })

            total_items = sum(len(c["contents"]) for c in containers)
            return {
                "containers": containers,
                "total_items": total_items
            }

        return raw_data

    def find_location(self, item_name: str) -> Dict[str, Any]:
        """Find where an item is located.

        Args:
            item_name: Name of the item to find

        Returns:
            Dictionary with locations where the item is found.
        """
        response = self._client.find_location(item_name)
        raw_data = self._handle_response(response)

        # Transform if needed - Java API may return list of container IDs or dict
        if isinstance(raw_data, list):
            if not raw_data:
                return {"found": False, "locations": []}

            locations = []
            for container_id in raw_data:
                locations.append({
                    "container_id": str(container_id),
                    "container_name": str(container_id)
                })

            return {
                "found": True,
                "locations": locations
            }

        return raw_data

    def get_container(self, container_id: str) -> Dict[str, Any]:
        """Get contents of a specific container.

        Args:
            container_id: ID or name of the container

        Returns:
            Dictionary with container contents.
        """
        response = self._client.get_container(container_id)
        raw_data = self._handle_response(response)

        # Transform if needed - Java API may return just a list
        if isinstance(raw_data, list):
            return {
                "container_id": container_id,
                "container_name": container_id,
                "contents": raw_data
            }

        return raw_data

    def create_items(
        self,
        container: str,
        items: List[str],
        dry_run: bool = False
    ) -> Dict[str, Any]:
        """Add items to a container.

        Args:
            container: Container ID or name
            items: List of item descriptions (e.g., ["3 hammers", "box of nails"])
            dry_run: If True, preview what would be added without executing

        Returns:
            Dictionary with operation result and updated container contents.
        """
        if dry_run:
            # For dry run, get current container state and simulate
            current = self.get_container(container)
            return {
                "dry_run": True,
                "container": container,
                "items_to_add": items,
                "current_contents": current.get("contents", []),
                "message": f"Would add {len(items)} item(s) to {container}"
            }

        response = self._client.create_items(container, items)
        return self._handle_response(response)

    def delete_items(
        self,
        container: str,
        items: List[str],
        dry_run: bool = False
    ) -> Dict[str, Any]:
        """Remove items from a container.

        Args:
            container: Container ID or name
            items: List of item descriptions to remove
            dry_run: If True, preview what would be removed without executing

        Returns:
            Dictionary with operation result and updated container contents.
        """
        if dry_run:
            # For dry run, get current container state and simulate
            current = self.get_container(container)
            return {
                "dry_run": True,
                "container": container,
                "items_to_remove": items,
                "current_contents": current.get("contents", []),
                "message": f"Would remove {len(items)} item(s) from {container}"
            }

        response = self._client.delete_items(container, items)
        return self._handle_response(response)

    def get_containers(self) -> List[Dict[str, Any]]:
        """Get list of all available containers.

        Returns:
            List of container dictionaries with id, name, and item count.
        """
        inventory = self.get_inventory()
        containers = inventory.get("containers", [])

        # Normalize container data
        normalized = []
        for container in containers:
            normalized.append({
                "id": container.get("id") or container.get("container_id"),
                "name": container.get("name") or container.get("container_name"),
                "item_count": len(container.get("contents", [])),
                "contents": container.get("contents", [])
            })

        return normalized

    def create_container(
        self,
        name: str,
        description: Optional[str] = None,
        dry_run: bool = False
    ) -> Dict[str, Any]:
        """Create a new empty container.

        Args:
            name: Container ID or name
            description: Optional location description
            dry_run: If True, preview without executing

        Returns:
            Dictionary with operation result; dry-run results include a
            "conflict" flag when the name is already taken.
        """
        if dry_run:
            containers = self.get_containers()
            conflict = any(
                c.get("id") == name or c.get("name") == name
                for c in containers
            )
            return {
                "dry_run": True,
                "container": name,
                "description": description,
                "conflict": conflict,
                "message": (
                    f"Container '{name}' already exists."
                    if conflict
                    else f"Would create container '{name}'"
                )
            }

        response = self._client.create_container(name, description)
        return self._handle_response(response)

    def rename_container(
        self,
        container_id: str,
        new_name: Optional[str] = None,
        description: Optional[str] = None,
        dry_run: bool = False
    ) -> Dict[str, Any]:
        """Rename a container and/or update its location description.

        Args:
            container_id: Current container ID or name
            new_name: New ID or name (omit for description-only update)
            description: New location description (omit to keep current)
            dry_run: If True, preview without executing

        Returns:
            Dictionary with operation result; dry-run results include
            "item_count" (items that would move) or "not_found".
        """
        if dry_run:
            # First check if container exists (get_container returns 404 for both
            # "not found" and "empty container", so we disambiguate via get_containers)
            containers = self.get_containers()
            container_exists = any(
                c.get("id") == container_id or c.get("name") == container_id
                for c in containers
            )
            if not container_exists:
                return {
                    "dry_run": True,
                    "container_id": container_id,
                    "new_name": new_name,
                    "description": description,
                    "not_found": True,
                    "message": f"Container '{container_id}' not found."
                }
            # Container exists — get its item count (may be 0 for empty containers)
            try:
                current = self.get_container(container_id)
                item_count = len(current.get("contents", []))
            except InventoryAPIError:
                # Container exists but API returned 404 (empty container case)
                item_count = 0
            return {
                "dry_run": True,
                "container_id": container_id,
                "new_name": new_name,
                "description": description,
                "item_count": item_count,
                "message": f"Would move {item_count} item(s) to '{new_name or container_id}'"
            }

        response = self._client.rename_container(container_id, new_name, description)
        return self._handle_response(response)


# Singleton instance for reuse
_client_instance: Optional[MCPInventoryClient] = None


def get_inventory_client() -> MCPInventoryClient:
    """Get or create singleton inventory client instance.

    Returns:
        MCPInventoryClient instance
    """
    global _client_instance
    if _client_instance is None:
        _client_instance = MCPInventoryClient()
    return _client_instance


def reset_client():
    """Reset the singleton client instance (useful for testing)."""
    global _client_instance
    _client_instance = None
