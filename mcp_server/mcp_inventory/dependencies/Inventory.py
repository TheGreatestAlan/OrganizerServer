from abc import ABC, abstractmethod

from mcp_inventory.dependencies.FunctionResponse import FunctionResponse


class Inventory(ABC):
    @abstractmethod
    def get_inventory(self) -> FunctionResponse:
        pass

    @abstractmethod
    def find_location(self, item_name: str) -> FunctionResponse:
        pass

    @abstractmethod
    def get_container(self, container_id: str) -> FunctionResponse:
        pass

    @abstractmethod
    def create_items(self, container: str, items: list) -> FunctionResponse:
        pass

    @abstractmethod
    def delete_items(self, container: str, items: list) -> FunctionResponse:
        pass

    @abstractmethod
    def create_container(self, name: str, description: str = None) -> FunctionResponse:
        pass

    @abstractmethod
    def rename_container(self, container_id: str, new_name: str = None, description: str = None) -> FunctionResponse:
        pass
