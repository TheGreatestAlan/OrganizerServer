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
    boolean result = inventory.createItemsInContainer(request.getContainer(), request.getItems());
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
    return buildResponse(
        inventory.deleteItemsFromContainer(request.getContainer(), request.getItems()));
  }

  private Response buildResponse(Object entity) {
    return Response.status(200)
        .entity(entity)
        .header("Access-Control-Allow-Origin", "*")
        .build();
  }

  public static class ItemContainerRequest {

    private String container;
    private List<String> items;

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
  }
}
