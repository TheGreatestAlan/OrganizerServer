package com.resources;

import com.services.Inventory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
  @Path("/{item}")
  public Response findLocation(@PathParam("item") String item) {
    return buildResponse(inventory.findItem(item));
  }

  private Response buildResponse(Object entity) {
    return Response.status(200)
        .entity(entity)
        .header("Access-Control-Allow-Origin", "*")
        .build();

  }

}
