package com.nguyen.server.resources;

import com.nguyen.server.services.Inventory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
public class InventoryResource {

    private final Inventory inventory;

    public InventoryResource(Inventory inventory) {
        this.inventory = inventory;
    }

    @GET
    public Response getInventory() {
        return buildResponse(inventory.getOrganizerInventory());
    }

    @GET
    @Path("/item/{itemName}")
    public Response findLocation(@PathParam("itemName") String itemName) {
        return buildResponse(inventory.findItem(itemName));
    }

    @GET
    @Path("/item/container/{containerId}")
    public Response getContainerItems(@PathParam("containerId") String containerId) {
        List<String> items = inventory.getContainerById(containerId);
        if (items.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Container not found or is empty.")
                    .build();
        }
        return buildResponse(items);
    }

    @GET
    @Path("/container/{containerId}")
    public Response findContainer(@PathParam("containerId") String containerId) {
        return buildResponse(inventory.findContainerLocation(containerId));
    }

    @POST
    @Path("/items")
    public Response createItems(ItemContainerRequest request) {
        if (request.getContainer() == null || request.getItems() == null || request.getItems().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Container identifier and items list must not be empty.")
                    .build();
        }

        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        boolean result = inventory.createItemsInContainer(request.getContainer(), request.getItems(), quantity);

        if (result) {
            return Response.status(Response.Status.CREATED)
                    .entity("Items successfully added.")
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Container not found.")
                    .build();
        }
    }

    @DELETE
    @Path("/items")
    public Response deleteItems(ItemContainerRequest request) {
        if (request.getContainer() == null || request.getItems() == null || request.getItems().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Container identifier and items list must not be empty.")
                    .build();
        }

        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        boolean result = inventory.deleteItemsFromContainer(request.getContainer(), request.getItems(), quantity);

        return buildResponse(result);
    }

    @POST
    @Path("/container")
    public Response createContainer(ContainerRequest request) {
        if (request == null || request.getId() == null || request.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Container id must not be empty.")
                    .build();
        }

        String containerId = request.getId().trim();
        if (inventory.containerExists(containerId)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Container '" + containerId + "' already exists.")
                    .build();
        }

        inventory.createContainer(containerId, request.getLocationDescription());
        return Response.status(Response.Status.CREATED)
                .entity("Container created.")
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @PUT
    @Path("/container/{containerId}")
    public Response renameContainer(@PathParam("containerId") String containerId,
                                    ContainerUpdateRequest request) {
        if (request == null
                || (isBlank(request.getNewId()) && isBlank(request.getLocationDescription()))) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Either newId or locationDescription must be provided.")
                    .build();
        }

        String trimmedContainerId = containerId.trim();
        String newId = isBlank(request.getNewId()) ? null : request.getNewId().trim();
        if (newId != null && !newId.equals(trimmedContainerId) && inventory.containerExists(newId)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("A container named '" + newId + "' already exists.")
                    .build();
        }

        boolean updated = inventory.renameContainer(
                trimmedContainerId, newId, request.getLocationDescription());
        if (!updated) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Container '" + trimmedContainerId + "' not found.")
                    .build();
        }
        return Response.status(Response.Status.OK)
                .entity("Container updated.")
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Response buildResponse(Object entity) {
        return Response.status(200)
                .entity(entity)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    public static class ContainerRequest {
        private String id;
        private String locationDescription;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLocationDescription() {
            return locationDescription;
        }

        public void setLocationDescription(String locationDescription) {
            this.locationDescription = locationDescription;
        }
    }

    public static class ContainerUpdateRequest {
        private String newId;
        private String locationDescription;

        public String getNewId() {
            return newId;
        }

        public void setNewId(String newId) {
            this.newId = newId;
        }

        public String getLocationDescription() {
            return locationDescription;
        }

        public void setLocationDescription(String locationDescription) {
            this.locationDescription = locationDescription;
        }
    }

    public static class ItemContainerRequest {
        private String container;
        private List<String> items;
        private Integer quantity;

        public String getContainer() {
            return container;
        }

        public void setContainer(String container) {
            this.container = container;
        }

        public List<String> getItems() {
            return items;
        }

        public void setItems(List<String> items) {
            this.items = items;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
