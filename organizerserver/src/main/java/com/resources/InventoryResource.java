package com.resources;

import com.evernote.EvernoteApi;
import com.services.Inventory;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
public class InventoryResource {

  private final Inventory inventory;

  public InventoryResource(Inventory inventory) {
    this.inventory = inventory;
  }

  @GET
  public List<String> getInventory() {
    return inventory.getOrganizerInventory();
  }

  @GET
  @Path("/{item}")
  public List<String> findLocation(@PathParam("item") String item) {
    return inventory.findItem(item);
  }
}
