from unittest.mock import MagicMock, patch

import pytest

from mcp_inventory.client.inventory_api import MCPInventoryClient, InventoryAPIError
from mcp_inventory.dependencies.FunctionResponse import FunctionResponse, Status


def make_client():
    with patch("mcp_inventory.client.inventory_api.InventoryClient"):
        return MCPInventoryClient(base_url="http://test")


class TestInventoryClientContainers:
    def test_create_container_posts_to_container_endpoint(self):
        from mcp_inventory.dependencies.InventoryRestClient import InventoryClient
        with patch.object(InventoryClient, "_make_request") as mock_make:
            mock_make.return_value = FunctionResponse(Status.SUCCESS, '"Container created."')
            client = InventoryClient("http://test")
            client.create_container("garage", "detached garage")

            args, kwargs = mock_make.call_args
            assert args[0] == "POST"
            assert args[1] == "http://test/inventory/container"
            assert kwargs["json"] == {"id": "garage", "locationDescription": "detached garage"}

    def test_rename_container_puts_to_container_endpoint(self):
        from mcp_inventory.dependencies.InventoryRestClient import InventoryClient
        with patch.object(InventoryClient, "_make_request") as mock_make:
            mock_make.return_value = FunctionResponse(Status.SUCCESS, '"Container updated."')
            client = InventoryClient("http://test")
            client.rename_container("old", "new", None)

            args, kwargs = mock_make.call_args
            assert args[0] == "PUT"
            assert args[1] == "http://test/inventory/container/old"
            assert kwargs["json"] == {"newId": "new", "locationDescription": None}


class TestMCPInventoryClientContainers:
    def test_create_container_success(self):
        client = make_client()
        client._client.create_container.return_value = FunctionResponse(
            Status.SUCCESS, '{"message": "Container created."}')

        result = client.create_container("garage", description="detached garage")

        assert result == {"message": "Container created."}
        client._client.create_container.assert_called_once_with("garage", "detached garage")

    def test_create_container_dry_run_no_mutation(self):
        client = make_client()
        client._client.get_inventory.return_value = FunctionResponse(
            Status.SUCCESS, '{"garage": ["hammer"]}')

        result = client.create_container("shed", dry_run=True)

        assert result["dry_run"] is True
        assert result["conflict"] is False
        client._client.create_container.assert_not_called()

    def test_create_container_dry_run_detects_conflict(self):
        client = make_client()
        client._client.get_inventory.return_value = FunctionResponse(
            Status.SUCCESS, '{"garage": ["hammer"]}')

        result = client.create_container("garage", dry_run=True)

        assert result["conflict"] is True

    def test_rename_container_success(self):
        client = make_client()
        client._client.rename_container.return_value = FunctionResponse(
            Status.SUCCESS, '{"message": "Container updated."}')

        result = client.rename_container("old", new_name="new", description=None)

        assert result == {"message": "Container updated."}
        client._client.rename_container.assert_called_once_with("old", "new", None)

    def test_rename_container_dry_run_reports_item_count(self):
        client = make_client()
        # Mock get_inventory for the existence check in dry-run
        client._client.get_inventory.return_value = FunctionResponse(
            Status.SUCCESS, '{"old": ["hammer", "wrench"]}')
        client._client.get_container.return_value = FunctionResponse(
            Status.SUCCESS, '["hammer", "wrench"]')

        result = client.rename_container("old", new_name="new", dry_run=True)

        assert result["dry_run"] is True
        assert result["item_count"] == 2
        client._client.rename_container.assert_not_called()

    def test_rename_container_dry_run_missing_source(self):
        client = make_client()
        # Mock get_inventory to return empty list (container doesn't exist)
        client._client.get_inventory.return_value = FunctionResponse(
            Status.SUCCESS, '{}')

        result = client.rename_container("missing", new_name="new", dry_run=True)

        assert result["not_found"] is True
