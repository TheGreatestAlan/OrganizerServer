<<<<<<<< HEAD:organizerserver/src/main/java/com/nguyen/server/resources/InventoryResource.java
package com.nguyen.server.resources;

import com.nguyen.server.services.Inventory;
========
package com.tippytappytoes.resources;

import com.tippytappytoes.services.Inventory;
>>>>>>>> local:organizerserver/src/main/java/com/tippytappytoes/resources/InventoryResource.java
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
  @Path("/item/{itemName}")
  public Response findLocation(@PathParam("itemName") String itemName) {
    return buildResponse(inventory.findItem(itemName));
  }

  @GET
  @Path("/container/{containerId}")
  public Response findContainer(@PathParam("containerId") String containerId) {
    return buildResponse(inventory.findContainerLocation(containerId));
  }

  private Response buildResponse(Object entity) {
    return Response.status(200)
        .entity(entity)
        .header("Access-Control-Allow-Origin", "*")
        .build();

  }

}
