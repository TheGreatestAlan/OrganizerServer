package com.nguyen.server.resources;

import com.nguyen.server.resources.InventoryResource.ContainerRequest;
import com.nguyen.server.resources.InventoryResource.ContainerUpdateRequest;
import com.nguyen.server.services.Inventory;
import com.nguyen.server.repository.H2InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.ws.rs.core.Response;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryResourceTest {

    private InventoryResource resource;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        String jdbcUrl = "jdbc:h2:file:" + tempDir.resolve("test.db").toString() + ";DB_CLOSE_DELAY=-1";
        resource = new InventoryResource(new Inventory(new H2InventoryRepository(jdbcUrl)));
    }

    private ContainerRequest createRequest(String id, String description) {
        ContainerRequest request = new ContainerRequest();
        request.setId(id);
        request.setLocationDescription(description);
        return request;
    }

    private ContainerUpdateRequest updateRequest(String newId, String description) {
        ContainerUpdateRequest request = new ContainerUpdateRequest();
        request.setNewId(newId);
        request.setLocationDescription(description);
        return request;
    }

    @Test
    void testCreateContainerReturns201() {
        Response response = resource.createContainer(createRequest("garage", "detached garage"));
        assertEquals(201, response.getStatus());
    }

    @Test
    void testCreateContainerDuplicateReturns409() {
        resource.createContainer(createRequest("garage", null));
        Response response = resource.createContainer(createRequest("garage", null));
        assertEquals(409, response.getStatus());
    }

    @Test
    void testCreateContainerEmptyIdReturns400() {
        Response response = resource.createContainer(createRequest("  ", null));
        assertEquals(400, response.getStatus());
    }

    @Test
    void testRenameContainerReturns200() {
        resource.createContainer(createRequest("old", null));
        Response response = resource.renameContainer("old", updateRequest("new", null));
        assertEquals(200, response.getStatus());
    }

    @Test
    void testRenameContainerNotFoundReturns404() {
        Response response = resource.renameContainer("missing", updateRequest("new", null));
        assertEquals(404, response.getStatus());
    }

    @Test
    void testRenameContainerTargetExistsReturns409() {
        resource.createContainer(createRequest("a", null));
        resource.createContainer(createRequest("b", null));
        Response response = resource.renameContainer("a", updateRequest("b", null));
        assertEquals(409, response.getStatus());
    }

    @Test
    void testRenameContainerEmptyBodyReturns400() {
        resource.createContainer(createRequest("a", null));
        Response response = resource.renameContainer("a", updateRequest(null, null));
        assertEquals(400, response.getStatus());
    }

    @Test
    void testRenameContainerMovesItems() {
        resource.createContainer(createRequest("old", null));
        InventoryResource.ItemContainerRequest itemRequest = new InventoryResource.ItemContainerRequest();
        itemRequest.setContainer("old");
        itemRequest.setItems(List.of("hammer"));
        resource.createItems(itemRequest);

        resource.renameContainer("old", updateRequest("new", null));

        Response contents = resource.getContainerItems("new");
        assertEquals(200, contents.getStatus());
    }
}
