import asyncio
from unittest.mock import MagicMock, patch

from mcp_inventory.client.inventory_api import InventoryAPIError


def run(coro):
    return asyncio.run(coro)


class TestCreateContainerTool:
    def test_success(self):
        from mcp_inventory.tools.containers import create_container
        mock_client = MagicMock()
        mock_client.create_container.return_value = {"message": "Container created."}

        with patch("mcp_inventory.tools.containers.get_inventory_client", return_value=mock_client):
            result = run(create_container("garage", description="detached garage"))

        assert "✅" in result
        assert "garage" in result
        assert "detached garage" in result

    def test_conflict(self):
        from mcp_inventory.tools.containers import create_container
        mock_client = MagicMock()
        mock_client.create_container.side_effect = InventoryAPIError(
            "Error 409: Container 'garage' already exists.")

        with patch("mcp_inventory.tools.containers.get_inventory_client", return_value=mock_client):
            result = run(create_container("garage"))

        assert "❌" in result
        assert "already exists" in result

    def test_dry_run(self):
        from mcp_inventory.tools.containers import create_container
        mock_client = MagicMock()
        mock_client.create_container.return_value = {
            "dry_run": True, "container": "shed", "description": None, "conflict": False}

        with patch("mcp_inventory.tools.containers.get_inventory_client", return_value=mock_client):
            result = run(create_container("shed", dry_run=True))

        assert "DRY RUN" in result
        assert "shed" in result

    def test_dry_run_conflict(self):
        from mcp_inventory.tools.containers import create_container
        mock_client = MagicMock()
        mock_client.create_container.return_value = {
            "dry_run": True, "container": "garage", "description": None, "conflict": True}

        with patch("mcp_inventory.tools.containers.get_inventory_client", return_value=mock_client):
            result = run(create_container("garage", dry_run=True))

        assert "DRY RUN" in result
        assert "already exists" in result


class TestRenameContainerTool:
    def test_success(self):
        from mcp_inventory.tools.containers import rename_container
        mock_client = MagicMock()
        mock_client.rename_container.return_value = {"message": "Container updated."}

        with patch("mcp_inventory.tools.containers.get_inventory_client", return_value=mock_client):
            result = run(rename_container("old", new_name="new"))

        assert "✅" in result
        assert "old" in result
        assert "new" in result

    def test_not_found(self):
        from mcp_inventory.tools.containers import rename_container
        mock_client = MagicMock()
        mock_client.rename_container.side_effect = InventoryAPIError(
            "Error 404: Container 'old' not found.")

        with patch("mcp_inventory.tools.containers.get_inventory_client", return_value=mock_client):
            result = run(rename_container("old", new_name="new"))

        assert "❌" in result
        assert "not found" in result

    def test_requires_new_name_or_description(self):
        from mcp_inventory.tools.containers import rename_container
        mock_client = MagicMock()

        with patch("mcp_inventory.tools.containers.get_inventory_client", return_value=mock_client):
            result = run(rename_container("old"))

        assert "❌" in result
        assert "new name" in result
        mock_client.rename_container.assert_not_called()

    def test_dry_run(self):
        from mcp_inventory.tools.containers import rename_container
        mock_client = MagicMock()
        mock_client.rename_container.return_value = {
            "dry_run": True, "container_id": "old", "new_name": "new", "item_count": 3}

        with patch("mcp_inventory.tools.containers.get_inventory_client", return_value=mock_client):
            result = run(rename_container("old", new_name="new", dry_run=True))

        assert "DRY RUN" in result
        assert "3" in result


class TestMetaToolRouting:
    def test_rename_routes_to_rename_tool(self):
        from mcp_inventory.server import inventory
        result = run(inventory("rename garage to shed"))
        assert "inventory_rename_container" in result

    def test_add_container_routes_to_create_tool(self):
        from mcp_inventory.server import inventory
        result = run(inventory("add a container named box"))
        assert "inventory_create_container" in result

    def test_add_items_still_routes_to_items_tool(self):
        from mcp_inventory.server import inventory
        result = run(inventory("add hammer to toolbox"))
        assert "inventory_create_items" in result
