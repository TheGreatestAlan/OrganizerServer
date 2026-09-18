import requests
from unittest.mock import patch

from mcp_inventory.dependencies.FunctionResponse import Status
from mcp_inventory.dependencies.InventoryRestClient import InventoryClient


def make_response(status_code, body_text):
    response = requests.Response()
    response.status_code = status_code
    response._content = body_text.encode()
    response.url = "http://test/inventory/container"
    return response


@patch("mcp_inventory.dependencies.InventoryRestClient.requests.request")
def test_http_error_surfaces_server_body(mock_request):
    conflict = make_response(409, "Container 'garage' already exists.")
    mock_request.return_value = conflict

    client = InventoryClient("http://test")
    result = client.create_items("garage", ["hammer"])

    assert result.status == Status.FAILURE
    assert "409" in result.response
    assert "Container 'garage' already exists." in result.response


@patch("mcp_inventory.dependencies.InventoryRestClient.requests.request")
def test_success_response_still_works(mock_request):
    ok = make_response(201, "Container created.")
    mock_request.return_value = ok

    client = InventoryClient("http://test")
    result = client.create_items("garage", ["hammer"])

    assert result.status == Status.SUCCESS
